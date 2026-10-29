package ma.enset.incidents.exception;

import org.springframework.http.HttpStatus;

/** Exceptions metier : chacune porte le code HTTP a renvoyer. */
public final class Exceptions {
    private Exceptions() {}

    public static class ApiException extends RuntimeException {
        private final HttpStatus status;
        public ApiException(HttpStatus status, String message) {
            super(message);
            this.status = status;
        }
        public HttpStatus getStatus() { return status; }
    }

    /** 404 */
    public static class NotFoundException extends ApiException {
        public NotFoundException(String message) { super(HttpStatus.NOT_FOUND, message); }
    }

    /** 409 : conflit (email deja utilise) */
    public static class ConflictException extends ApiException {
        public ConflictException(String message) { super(HttpStatus.CONFLICT, message); }
    }

    /** 403 : authentifie mais pas autorise */
    public static class ForbiddenException extends ApiException {
        public ForbiddenException(String message) { super(HttpStatus.FORBIDDEN, message); }
    }

    /** 401 : identifiants invalides / refresh token invalide */
    public static class UnauthorizedException extends ApiException {
        public UnauthorizedException(String message) { super(HttpStatus.UNAUTHORIZED, message); }
    }

    /** 400 : requete incorrecte / regle metier violee */
    public static class BadRequestException extends ApiException {
        public BadRequestException(String message) { super(HttpStatus.BAD_REQUEST, message); }
    }
}
