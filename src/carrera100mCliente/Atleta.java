package carrera100mCliente;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Atleta extends Thread {

	int dorsal;
	
	
	public Atleta(int dorsal) {
		this.dorsal = dorsal;
	}
	
	
	public void run() {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/Carrera100mServidor").build();
		WebTarget target = client.target(uri);
		
		try {
			MainCarrera.sem_salida.acquire();
			
			System.out.println(target.path("carrera100")
					.path("preparado")
					.request(MediaType.TEXT_PLAIN)
					.get(String.class));
			
			System.out.println(target
					.path("carrera100")
					.path("listo")
					.request(MediaType.TEXT_PLAIN)
					.get(String.class));

			Thread.sleep((long)(Math.random() * 2200 + 9560));
			
			System.out.println(target.path("carrera100")
					.path("llegada")
					.queryParam("dorsal", "" + this.dorsal)
					.request(MediaType.TEXT_PLAIN)
					.get(String.class));
			
			MainCarrera.sem_llegada.release();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
