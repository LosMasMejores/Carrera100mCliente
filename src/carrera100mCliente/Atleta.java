package carrera100mCliente;

import java.net.URI;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class Atleta extends Thread {

	int dorsal;
	Semaphore sem_salida;
	Semaphore sem_llegada;
	
	
	public Atleta(int dorsal, Semaphore sem_salida, Semaphore sem_llegada) {
		this.dorsal = dorsal;
		this.sem_salida = sem_salida;
		this.sem_llegada = sem_llegada;
	}
	
	
	public void run() {
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/Carrera100mServidor").build();
		WebTarget target = client.target(uri);
		
		try {
			this.sem_salida.acquire(); // Esperar por MainCarrera
			
			System.out.println(target.path("carrera100")
					.path("preparado")
					.request(MediaType.TEXT_PLAIN)
					.get(String.class)); // Llamar al servicio /preparado
			
			System.out.println(target
					.path("carrera100")
					.path("listo")
					.request(MediaType.TEXT_PLAIN)
					.get(String.class)); // Llamar al servicio /listo

			Thread.sleep((long)(Math.random() * 2200 + 9560)); // Correr
			
			System.out.println(this.dorsal + " he tardado " + target.path("carrera100")
					.path("llegada")
					.queryParam("dorsal", "" + this.dorsal)
					.request(MediaType.TEXT_PLAIN)
					.get(String.class)); // Llamar al servicio /llegada con nuestro dorsal
			
			this.sem_llegada.release(); // Avisar a MainCarrera
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
