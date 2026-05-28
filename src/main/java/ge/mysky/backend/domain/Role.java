package ge.mysky.backend.domain;

public enum Role {
    ADMIN,
    WORKER;

    public String authority() {
        return "ROLE_" + name();
    }
}
