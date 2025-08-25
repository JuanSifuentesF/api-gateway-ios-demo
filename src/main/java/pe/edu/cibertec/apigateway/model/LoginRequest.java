package pe.edu.cibertec.apigateway.model;

public class LoginRequest {
    private String email;
    private String password;

    // Constructor por defecto
    public LoginRequest() {}

    // Constructor con parámetros
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters y setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
