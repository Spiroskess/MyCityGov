package gr.hua.dit.mycitygov.config;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.Role;
import gr.hua.dit.mycitygov.core.repository.CitizenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitConfig {

    @Bean
    public CommandLineRunner initDefaultUsers(CitizenRepository citizenRepository,
                                              PasswordEncoder passwordEncoder) {
        return args -> {


            //DEFAULT EMPLOYEE USER

            String employeeEmail = "employee@mycitygov.gr";

            if (citizenRepository.findByEmail(employeeEmail).isEmpty()) {
                Citizen employee = new Citizen();
                employee.setEmail(employeeEmail);
                employee.setPassword(passwordEncoder.encode("employee123"));
                employee.setFullName("Default Employee");
                employee.setPhone("2104444444");
                employee.setRole(Role.EMPLOYEE);

                citizenRepository.save(employee);

                System.out.println(
                    "\n------------------------------------------" +
                        "\nCreated default EMPLOYEE user:" +
                        "\nEmail:    " + employeeEmail +
                        "\nPassword: employee123" +
                        "\n------------------------------------------\n"
                );
            }


            //DEFAULT ADMIN USER

            String adminEmail = "admin@mycitygov.gr";

            if (citizenRepository.findByEmail(adminEmail).isEmpty()) {
                Citizen admin = new Citizen();
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setFullName("System Administrator");
                admin.setPhone("2100000000");
                admin.setRole(Role.ADMIN);

                citizenRepository.save(admin);

                System.out.println(
                    "\n------------------------------------------" +
                        "\nCreated default ADMIN user:" +
                        "\nEmail:    " + adminEmail +
                        "\nPassword: admin123" +
                        "\n------------------------------------------\n"
                );
            }
        };
    }
}
