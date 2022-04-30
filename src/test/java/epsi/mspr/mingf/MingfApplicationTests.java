package epsi.mspr.mingf;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import epsi.mspr.mingf.controllers.AuthController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.DirtiesContext;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest(properties = "spring.main.lazy-initialization=true",
		classes = {MingfApplicationTests.class})
class MingfApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	public void givenIP_whenFetchingCity_thenReturnsCityData()
			throws IOException, GeoIp2Exception {
		ArrayList<String> ips = new ArrayList<String>();
		ips.add("37.8.160.0");//free mobile
		ips.add("8.8.8.8");
		ips.add("217.182.0.0");//OVH SAS

		ArrayList<String> countries = new ArrayList<String>();
		countries.add("France");//free mobile
		countries.add("United States");
		countries.add("France");//OVH SAS
		AuthController authController = new AuthController();
		for ( String ip : ips){
			String countryName = authController.givenIP_ReturnsCityData(ip);
			System.out.println("Adresse IP ("+ ip + ") est l'origine de " + countryName);
			Assertions.assertEquals(countries.get(ips.indexOf(ip)), countryName);
		}
	}
}
