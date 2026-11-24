package cl.ferreteria.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cl.ferreteria.ventas", "cl.ferreteria.shared"})
public class VentasApplication {
    public static void main(String[] args) {
        SpringApplication.run(VentasApplication.class, args);
    }
}
