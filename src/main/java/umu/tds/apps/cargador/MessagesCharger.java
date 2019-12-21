package umu.tds.apps.cargador;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import umu.tds.apps.whatsappparser.MensajeWhatsApp;
import umu.tds.apps.whatsappparser.Plataforma;
import umu.tds.apps.whatsappparser.SimpleTextParser;

public class MessagesCharger implements Serializable {
	public static String IOS = "IOS";
	public static String ANDROID_1 = "ANDROID_1";
	public static String ANDROID_2 = "ANDROID_2";
	public static String IOS_DATE = "d/M/yy H:mm:ss";
	public static String ANDROID_1_DATE = "d/M/yy H:mm";
	public static String ANDROID_2_DATE = "d/M/yyyy H:mm";
	
	private static final long serialVersionUID = 1L;
	private List<MessagesListener> observadores;
	private List<MensajeWhatsApp> listaMensajes;

	// Constructores
	public MessagesCharger() {
		this(new LinkedList<>());
	}

	public MessagesCharger(List<MensajeWhatsApp> listaMensajes) {
		this.listaMensajes = listaMensajes;
		observadores = new LinkedList<>();
	}

	// Metodos get y set
	public List<MensajeWhatsApp> getListaMensajes() {
		return listaMensajes;
	}

	public void setFichero(String path, Plataforma plataforma, String dateFormat) {
		List<MensajeWhatsApp> chat;
		try {
			chat = SimpleTextParser.parse(path, dateFormat, plataforma);

			if (!listaMensajes.equals(chat)) {
				MessagesEvent evento = new MessagesEvent(this, listaMensajes, chat);
				listaMensajes = chat;
				notificarCambioMensajes(evento);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Recorre la lista de oyentes y notifica del cambio
	private void notificarCambioMensajes(MessagesEvent evento) {
		// Para evitar problemas de concurrencia me creo una copia
		List<MessagesListener> copia = null;
		synchronized (observadores) {
			copia = new LinkedList<>(observadores);
		}

		for (MessagesListener observador : copia) {
			observador.nuevosMensajes(evento);
		}
	}

	// Manejo de los oyentes
	public synchronized void addListener(MessagesListener observador) {
		this.observadores.add(observador);
	}

	public synchronized void removeListener(MessagesListener observador) {
		this.observadores.remove(observador);
	}

}
