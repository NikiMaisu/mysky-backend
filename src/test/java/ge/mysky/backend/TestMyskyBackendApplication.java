package ge.mysky.backend;

import org.springframework.boot.SpringApplication;

public class TestMyskyBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(MyskyBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
