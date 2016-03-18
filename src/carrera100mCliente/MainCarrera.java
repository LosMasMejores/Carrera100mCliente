package carrera100mCliente;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

public class MainCarrera {	
	
	// Funcion sacada de stackoverflow para ordenar mapas por el valor
	public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
	{
		Map<K,V> result = new LinkedHashMap<>();
		Stream <Entry<K,V>> st = map.entrySet().stream();
		
		st.sorted(Comparator.comparing(e -> e.getValue()))
			.forEachOrdered(e ->result.put(e.getKey(),e.getValue()));
		
		return result;
	}
	
	
	public static void main(String[] args) {
		
		Semaphore sem_salida;
		Semaphore sem_llegada;
		ArrayList<Atleta> atletas = new ArrayList<>();
		int dorsal;
		
		int num_carreras = 1;
		int num_atletas = 4;
		
		if (args.length == 0) {
			System.out.println("USO: java mainCarrera [carreras] [atletas]");
			System.out.println("\t[carreras] = numero de carreras participantes");
			System.out.println("\t[atletas] = atletas por carrera");
			return;
		} else if (args.length > 2) {
			System.out.println("ERROR: numero de parametros incorrectos");
			return;
		} else if (Integer.parseInt(args[0]) <= 0 || Integer.parseInt(args[1]) <= 0) {
			System.out.println("ERROR: valor de los parametros <= 0");
			return;
		} else {
			num_carreras = Integer.parseInt(args[0]);
			num_atletas = Integer.parseInt(args[1]);
		}
		
		Client client = ClientBuilder.newClient();
		URI uri = UriBuilder.fromUri("http://localhost:8080/Carrera100mServidor").build();
		WebTarget target = client.target(uri);
		
		String respuesta = (target.path("carrera100")
				.path("reinicio")
				.queryParam("carreras", "" + num_carreras)
				.queryParam("atletas", "" + num_atletas)
				.request(MediaType.TEXT_PLAIN)
				.get(String.class)); // Llamar al servicio /reinicio
		
		if (respuesta.contentEquals("COMPLETO") || respuesta.contentEquals("INCORRECTO")) {
			System.out.println("ERROR: " + respuesta);
			return;
		} else {
			dorsal = Integer.parseInt(respuesta); // Determinar que dorsales repartimos con la respuesta
		}
		
		sem_salida = new Semaphore(num_atletas);
		sem_llegada = new Semaphore(num_atletas);
		
		for (int i = 0; i < num_atletas; i++) {
			atletas.add(new Atleta(dorsal-i, sem_salida, sem_llegada));
		}
		
		try {
			
			sem_llegada.acquire(num_atletas); // Iniciamos los semaforos para coordinar los corredores
			sem_salida.acquire(num_atletas);  //
			
			for (int i = 0; i < num_atletas; i++) {
				atletas.get(i).start();
			}
			
			sem_salida.release(num_atletas); // Iniciamos la carrera
			sem_llegada.acquire(num_atletas); // Esperamos a que terminen los corredores
			
			Resultado resultado = target.path("carrera100")
					.path("resultados")
					.request(MediaType.APPLICATION_XML_TYPE)
					.get(Resultado.class); // Llamamos al servicio /resultados
			
			resultado.map = sortByValue(resultado.map); // Ordenamos los resultados
			
			for (Map.Entry<Integer, Long> entry: resultado.map.entrySet()) {
				System.out.println("Dorsal: " + entry.getKey() + ", tiempo: " + entry.getValue());
			}
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
