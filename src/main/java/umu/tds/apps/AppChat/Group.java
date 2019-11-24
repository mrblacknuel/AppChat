package umu.tds.apps.AppChat;

import java.util.List;

public class Group extends Contact {
	// Properties.
	private List<Contact> contactos;
	private User admin;
	
	// Constructor.
	public Group(String nombre, List<Message> mensajes, List<Contact> contactos, User admin) {
		super(nombre, mensajes);
		this.contactos = contactos;
		this.admin = admin;
	}
	
	//Getters
	public List<Contact> getContactos() {
		return contactos;
	}

	public User getAdmin() {
		return admin;
	}
}