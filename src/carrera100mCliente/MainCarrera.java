package carrera100mCliente;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class MainCarrera {

	public static void main(String[] args) {

		int num_atletas = 4;
		Semaphore sem_salida;
		Semaphore sem_llegada;
		ArrayList<Atleta> atletas = new ArrayList<>();
		
		sem_salida = new Semaphore(num_atletas);
		sem_llegada = new Semaphore(num_atletas);
		
		if (args.length > 1) {
			return;
		}
		if (args.length == 1) {
			num_atletas = Integer.parseInt(args[0]);
			if (num_atletas <= 0){
				return;
			}
		}
		
		for (int i = 0; i < num_atletas; i++) {
			atletas.add(new Atleta(i+1, sem_salida, sem_llegada));
		}
		
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
			for (int i = 0; i < num_atletas; i++) {
				atletas.get(i).start();
			}
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
