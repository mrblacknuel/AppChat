package umu.tds.apps.controlador;

import java.awt.Color;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import tds.BubbleText;
import umu.tds.apps.AppChat.*;
import umu.tds.apps.persistencia.DAOException;
import umu.tds.apps.persistencia.FactoriaDAO;
import umu.tds.apps.persistencia.GroupDAO;
import umu.tds.apps.persistencia.IndividualContactDAO;
import umu.tds.apps.persistencia.MessageDAO;
import umu.tds.apps.persistencia.StatusDAO;
import umu.tds.apps.persistencia.UserDAO;

public class Controlador {
	// Instancia del controlador.
	private static Controlador unicaInstancia = null;

	// Adaptadores
	private GroupDAO adaptadorGrupo;
	private IndividualContactDAO adaptadorContactoIndividual;
	private MessageDAO adaptadorMensaje;
	private UserDAO adaptadorUsuario;
	private StatusDAO adaptadorEstado;

	// Catálogos
	private UsersCatalogue catalogoUsuarios;

	// Nuestro usuario.
	private User usuarioActual;

	private Controlador() {
		inicializarAdaptadores(); // debe ser la primera linea para evitar error de sincronización
		inicializarCatalogos();
	}

	// Aplicamos el patrón Singleton.
	// Consiguiendo de esta forma que exista una única instancia de la clase
	// controlador que es accesible globalmente.
	public static Controlador getInstancia() {
		if (unicaInstancia == null)
			unicaInstancia = new Controlador();
		return unicaInstancia;
	}

	// Inicializamos los adaptadores
	private void inicializarAdaptadores() {
		FactoriaDAO factoria = null;
		try {
			factoria = FactoriaDAO.getInstancia(FactoriaDAO.DAO_TDS);
		} catch (DAOException e) {
			e.printStackTrace();
		}

		adaptadorGrupo = factoria.getGrupoDAO();
		adaptadorContactoIndividual = factoria.getContactoIndividualDAO();
		adaptadorMensaje = factoria.getMensajeDAO();
		adaptadorUsuario = factoria.getUserDAO();
		adaptadorEstado = factoria.getEstadoDAO();
	}

	// Inicializamos los catálogos
	private void inicializarCatalogos() {
		catalogoUsuarios = UsersCatalogue.getUnicaInstancia();
	}

	public boolean iniciarSesion(String nick, String password) {
		if (nick.isEmpty() || password.isEmpty()) {
			return false;
		}

		User cliente = catalogoUsuarios.getUsuario(nick);

		if (cliente == null)
			return false;

		// Si la password esta bien inicia sesion
		if (cliente.getPassword().equals(password)) {
			usuarioActual = cliente;
			return true;
		}
		return false;
	}

	// Registro el usuario. Devuelvo false si el nick ya está en uso
	public boolean crearCuenta(ImageIcon imagen, String nick, String password, String email, String name,
			int numTelefono, LocalDate fechaNacimiento) {
		User u = catalogoUsuarios.getUsuario(nick);
		if (u == null) {
			User nuevoUsuario = new User(imagen, name, fechaNacimiento, numTelefono, nick, password, false, null, null);
			catalogoUsuarios.addUsuario(nuevoUsuario);
			adaptadorUsuario.registrarUsuario(nuevoUsuario);
			return true;
		}
		return false;
	}

	public User getUsuarioActual() {
		return usuarioActual;
	}

	public void setSaludoUsuario(String saludo) {
		usuarioActual.setSaludo(saludo);
	}

	public void addImagenUsuario(ImageIcon image) { // MANUELITO
		// TODO añadir una imagen al conjunto de imágenes del usuario
		usuarioActual.addProfilePhoto(image);
	}

	// Devuelvo mi lista de contactos.
	public List<Contact> getContactosUsuarioActual() {
		if (usuarioActual == null)
			return new LinkedList<Contact>();
		User u = catalogoUsuarios.getUsuario(usuarioActual.getCodigo());
		return u.getContactos();
	}

	// Devuelvo el último mensaje con ese contacto.
	public Message getUltimoMensaje(Contact contacto) {
		if (contacto.getMensajes().isEmpty())
			return null;
		return contacto.getMensajes().get(contacto.getMensajes().size() - 1);
	}

	// Devuelvo mi lista de mensajes con ese contacto
	public List<Message> getMensajes(Contact contacto) {
		List<Message> mensajesEnviados = new LinkedList<>();
		List<Message> mensajesRecibidos = new LinkedList<>();
		
		// Obtengo los mensajes que he enviado a ese grupo o contacto individual
		if (contacto instanceof Group) {
			mensajesEnviados = ((Group) contacto).getMensajesAdmin();
			mensajesRecibidos = ((Group) contacto).getMensajesRecibidos();
		} else {
			mensajesEnviados = contacto.getMensajes();
			mensajesRecibidos = ((IndividualContact) contacto).getMensajesRecibidos(usuarioActual);
		}

		mensajesEnviados.addAll(mensajesRecibidos);
		return Stream.concat(mensajesEnviados.stream(), mensajesRecibidos.stream())
				.sorted((m1, m2) -> m1.getHora().compareTo(m2.getHora())).collect(Collectors.toList());
	}

	// Creo el contacto. Da error si tiene como nombre el de otro ya creado.
	public boolean crearContacto(String nombre, int numTelefono) {
		boolean existeContacto = false;
		if (!usuarioActual.getContactos().isEmpty()) {
			existeContacto = usuarioActual.getContactos().stream()
					.filter(c -> c instanceof IndividualContact)
					.map(c -> (IndividualContact) c)
					.anyMatch(c -> c.getNombre().equals(nombre));
		}
		
		if (!existeContacto)  {
			User usuario = catalogoUsuarios.getUsuarios().stream().filter(u -> u.getNumTelefono() == numTelefono).collect(Collectors.toList()).get(0);
			IndividualContact nuevoContacto = new IndividualContact(nombre, new LinkedList<Message>(), numTelefono, usuario);
			usuarioActual.addContacto(nuevoContacto);
			adaptadorContactoIndividual.registrarContacto(nuevoContacto);
			return true;
		}
		return false;
	}

	// Creo el grupo.
	public void crearGrupo(String nombre, List<IndividualContact> participantes) {
		Group nuevoGrupo = new Group(nombre, new LinkedList<Message>(), participantes, usuarioActual);
		usuarioActual.addGrupo(nuevoGrupo);
		usuarioActual.addGrupoAdmin(nuevoGrupo);
		adaptadorGrupo.registrarGrupo(nuevoGrupo);
	}

	public List<Group> getGruposAdminUsuarioActual() {
		// Devuelvo una lista de mis grupos. Saco el código del usuario actual.
		return usuarioActual.getGruposAdmin();
	}

	// Devuelvo una lista con los nombres de los grupos en los que se usuario y yo estamos.
	public List<String> getNombresGrupo(IndividualContact contacto) {
		return usuarioActual.getContactos().stream()
			.filter(c -> c instanceof Group)
			.map(c -> (Group) c)
			.filter(g -> g.getContactos().stream().anyMatch(c -> c.getNombre().equals(contacto.getNombre())))
			.map(g -> g.getNombre())
			.collect(Collectors.toList());
	}

	public void hacerPremium() { // MANUELITO
		// TODO Ponerle algun descuento segun convenga
		usuarioActual.setPremium();
	}

	public void cerrarSesion() { // MANUELITO
		usuarioActual = null;
	}

	public List<Message> buscarMensajes(String emisor, LocalDate fechaInicio, LocalDate fechaFin, String text) { // MANUELITO
		// Recupero los mensajes que he enviado
		List<Message> enviados = usuarioActual.getContactos().stream().flatMap(c -> c.getMensajes().stream())
				.collect(Collectors.toList());

		// Recupero los mensajes que he recibido
		List<Message> recibidos = usuarioActual.getContactos().stream().flatMap(c -> {
			List<Message> m;
			if (c instanceof IndividualContact)
				m = ((IndividualContact) c).getMensajesRecibidos(usuarioActual);
			else
				m = ((Group) c).getMensajesRecibidos();
			return m.stream();
		}).collect(Collectors.toList());

		return Stream.concat(recibidos.stream(), enviados.stream())
				.filter(m -> emisor == null || m.getEmisor().getName().equals(emisor))
				.filter(m -> emisor == null || m.getHora().isAfter(fechaInicio) && m.getHora().isBefore(fechaFin))
				.filter(m -> emisor == null || m.getTexto().contains(text)).collect(Collectors.toList());
	}

	// Borramos el contacto
	public void deleteContact(Contact c) {
		usuarioActual.removeContact(c);
		if (c instanceof IndividualContact) {
			adaptadorContactoIndividual.borrarContacto((IndividualContact) c);
		} else {
			adaptadorGrupo.borrarGrupo((Group) c);
		}
	}
	
	public void addEstado() {	// ALFONSITO
		
	}
	
	public List<Status> getEstados(List<Contact> contactos) {	// ALFONSITO 
		return null;
	}
	
	public void enviarMensaje() { // MANUELITO
		
	}

	public static List<BubbleText> getChat(User u, JPanel chat) {
		List<BubbleText> list = new LinkedList<BubbleText>();
		BubbleText burbuja = new BubbleText(chat, "Hola, ¿Como van las burbujas? xD", Color.LIGHT_GRAY, "Dieguin",
				BubbleText.RECEIVED);
		list.add(burbuja);
		return list;
	}
}
