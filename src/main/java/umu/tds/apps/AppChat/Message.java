package umu.tds.apps.AppChat;

import java.time.LocalDateTime;

public class Message implements Comparable<Message> {
	// Properties.
	private int codigo;
	private String texto;
	private LocalDateTime hora;
	private int emoticono;
	private User emisor;
	private Contact receptor;

	// Constructor.
	public Message(String texto, LocalDateTime hora, User emisor, Contact receptor) {
		this.texto = texto;
		this.hora = hora;
		this.setEmisor(emisor);
		this.setReceptor(receptor);
	}

	public Message(int emoticono, LocalDateTime hora, User emisor, Contact receptor) {
		this.texto = "";
		this.hora = hora;
		this.emoticono = emoticono;
		this.setEmisor(emisor);
		this.setReceptor(receptor);
	}

	public Message(String texto, int emoticono, LocalDateTime hora) {
		this.texto = texto;
		this.emoticono = emoticono;
		this.hora = hora;
	}

	// Getters.
	public String getTexto() {
		return texto;
	}

	public LocalDateTime getHora() {
		return hora;
	}

	public int getEmoticono() {
		return emoticono;
	}

	public User getEmisor() {
		return emisor;
	}

	public Contact getReceptor() {
		return receptor;
	}

	public int getCodigo() {
		return codigo;
	}

	public void setCodigo(int codigo) {
		this.codigo = codigo;
	}

	public void setReceptor(Contact receptor) {
		this.receptor = receptor;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public void setEmisor(User emisor) {
		this.emisor = emisor;
	}

	@Override
	public int compareTo(Message o) {
		return hora.compareTo(o.hora);
	}
}
