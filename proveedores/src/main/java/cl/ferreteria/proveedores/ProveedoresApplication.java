package cl.ferreteria.proveedores;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cl.ferreteria.proveedores", "cl.ferreteria.shared"})
public class ProveedoresApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProveedoresApplication.class, args);
    }
}
