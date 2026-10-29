#!/usr/bin/env python3
"""Rejoue le scenario (section 24) et les tests de securite (section 25). Backend demarre sur :8080.
Aucune dependance externe (bibliotheque standard uniquement)."""
import base64, hashlib, hmac, json, sys, time, urllib.request, urllib.error, uuid

BASE = "http://localhost:8080"
SECRET = "change-me-please-this-secret-must-be-at-least-32-bytes-long"  # = valeur par defaut de application.properties
ok_count = ko_count = 0


def call(method, path, body=None, token=None):
    data = json.dumps(body).encode() if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", f"Bearer {token}")
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read()
            return r.status, (json.loads(raw) if raw else None)
    except urllib.error.HTTPError as e:
        raw = e.read()
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw.decode(errors="ignore")


def check(label, got, expected):
    global ok_count, ko_count
    good = got == expected
    ok_count += good
    ko_count += not good
    print(f"[{'OK' if good else 'KO'}] {label}: HTTP {got} (attendu {expected})")


def login(email, pwd):
    s, d = call("POST", "/auth/login", {"email": email, "password": pwd})
    assert s == 200, f"login {email} -> {s} {d}"
    return d["token"]


def b64(b):
    return base64.urlsafe_b64encode(b).rstrip(b"=").decode()


def forge_expired_token(email, role):
    """Fabrique un JWT HS256 valide mais deja expire (meme cle que le serveur de dev)."""
    now = int(time.time())
    header = b64(json.dumps({"alg": "HS256"}).encode())
    payload = b64(json.dumps({"sub": email, "role": role, "iat": now - 600, "exp": now - 300}).encode())
    sig = b64(hmac.new(SECRET.encode(), f"{header}.{payload}".encode(), hashlib.sha256).digest())
    return f"{header}.{payload}.{sig}"


run = uuid.uuid4().hex[:6]
print("=== Scenario ===")
e1 = f"emp1_{run}@test.ma"
e2 = f"emp2_{run}@test.ma"
for e in (e1, e2):
    s, d = call("POST", "/auth/register", {"fullName": "Employe " + e[:4], "email": e, "password": "Password123!"})
    check(f"inscription {e}", s, 201)
    assert d["role"] == "EMPLOYEE"

admin = login("admin@demo.ma", "Admin123!")
tech = login("tech1@demo.ma", "Tech1234!")
tech2 = login("tech2@demo.ma", "Tech1234!")
t1 = login(e1, "Password123!")
t2 = login(e2, "Password123!")

inc = {"title": "PC infecte", "description": "Comportement suspect", "category": "MALWARE", "priority": "HIGH"}
s, a1 = call("POST", "/api/incidents", inc, t1); check("employe1 cree incident 1", s, 201)
s, a2 = call("POST", "/api/incidents", inc, t1); check("employe1 cree incident 2", s, 201)
s, b1 = call("POST", "/api/incidents", inc, t2); check("employe2 cree incident", s, 201)
s, _ = call("GET", f"/api/incidents/{b1['id']}", token=t1); check("employe1 lit l'incident d'employe2 (interdit)", s, 403)
s, lst = call("GET", "/api/incidents", token=admin); check("admin liste tous les incidents", s, 200)
s, techs = call("GET", "/api/admin/technicians", token=admin)
tech_id = next(t["id"] for t in techs if t["email"] == "tech1@demo.ma")
s, _ = call("PUT", f"/api/incidents/{a1['id']}/assign/{tech_id}", token=admin); check("admin affecte l'incident", s, 200)
s, mine = call("GET", "/api/incidents/assigned", token=tech); check("technicien liste ses incidents", s, 200)
s, _ = call("PUT", f"/api/incidents/{a1['id']}/status", {"status": "IN_PROGRESS"}, tech); check("-> IN_PROGRESS", s, 200)
s, _ = call("PUT", f"/api/incidents/{a1['id']}/status", {"status": "RESOLVED"}, tech); check("-> RESOLVED", s, 200)
s, _ = call("POST", f"/api/incidents/{a1['id']}/comments", {"content": "Malware supprime."}, tech); check("note de traitement", s, 201)
s, det = call("GET", f"/api/incidents/{a1['id']}", token=t1); check("employe1 voit son incident + historique", s, 200)
print("   historique :", [h["message"] for h in det["history"]])

print("\n=== Tests de securite (section 25) ===")
s, _ = call("GET", "/api/profile"); check("1. API protegee sans JWT", s, 401)
s, _ = call("GET", "/api/profile", token="abc.def.ghi"); check("2. JWT invalide", s, 401)
s, _ = call("GET", "/api/profile", token=forge_expired_token(e1, "EMPLOYEE")); check("3. JWT expire", s, 401)
s, _ = call("GET", "/api/admin/users", token=t1); check("4. EMPLOYEE sur route ADMIN", s, 403)
s, _ = call("GET", f"/api/incidents/{b1['id']}", token=t1); check("5. EMPLOYEE sur incident d'un autre", s, 403)
s, _ = call("PUT", f"/api/incidents/{a1['id']}/status", {"status": "CLOSED"}, tech2); check("6. TECHNICIAN sur incident non affecte", s, 403)
s, users = call("GET", "/api/admin/users", token=admin)
uid = next(u["id"] for u in users if u["email"] == e2)
call("PUT", f"/api/admin/users/{uid}/disable", token=admin)
s, _ = call("POST", "/auth/login", {"email": e2, "password": "Password123!"}); check("7. utilisateur desactive : login", s, 403)
s, _ = call("GET", "/api/profile", token=t2); check("7b. utilisateur desactive : ancien JWT", s, 401)
s, _ = call("POST", "/auth/register", {"fullName": "Doublon", "email": e1, "password": "Password123!"}); check("8. email deja existant", s, 409)
s, d = call("POST", "/api/incidents", {"title": "", "description": "", "category": None, "priority": None}, t1); check("9. incident avec donnees invalides", s, 400)
print("   erreurs :", d.get("errors"))
s, d = call("POST", "/auth/register", {"fullName": "Hack", "email": f"h_{run}@test.ma", "password": "Password123!", "role": "ADMIN"})
print("   inscription avec role=ADMIN ->", d.get("role"), "(doit etre EMPLOYEE)")

print(f"\nResultat : {ok_count} OK, {ko_count} KO")
sys.exit(1 if ko_count else 0)
