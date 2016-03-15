package carrera100mCliente;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class MainCarrera {

	static Semaphore sem_salida = new Semaphore(4);
	static Semaphore sem_llegada = new Semaphore(4);
	

	public static void main(String[] args) {

		Atleta atleta_1 = new Atleta(1);
		Atleta atleta_2 = new Atleta(2);
		Atleta atleta_3 = new Atleta(3);
		Atleta atleta_4 = new Atleta(4);
		
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/Carrera100mServidor").build();
		WebTarget target = client.target(uri);
		
		System.out.println(target.path("carrera100")
				.path("reinicio")
				.request(MediaType.TEXT_PLAIN)
				.get(String.class));
		
		try {
			sem_llegada.acquire(4);
			sem_salida.acquire(4);
			atleta_1.start();
			atleta_2.start();
			atleta_3.start();
			atleta_4.start();
			sem_salida.release(4);
			sem_llegada.acquire(4);
			
			Resultado resultado = target.path("carrera100")
					.path("resultados")
					.request(MediaType.APPLICATION_XML_TYPE)
					.get(Resultado.class);
			
			for (Map.Entry<Integer, Long> entry: resultado.map.entrySet()) {
				System.out.println("Dorsal: " + entry.getKey() + ", tiempo: " + entry.getValue());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
