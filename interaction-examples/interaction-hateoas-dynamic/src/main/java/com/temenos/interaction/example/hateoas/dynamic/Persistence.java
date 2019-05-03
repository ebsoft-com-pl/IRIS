package com.temenos.interaction.example.hateoas.dynamic;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.List;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.example.hateoas.dynamic.model.Author;
import com.temenos.interaction.example.hateoas.dynamic.model.Note;

public class Persistence {
    private final static Logger logger = LoggerFactory.getLogger(Persistence.class);

    @PersistenceContext(unitName = "ResponderServiceHibernate", type = PersistenceContextType.EXTENDED)
    @Access(AccessType.FIELD) 
    private EntityManager entityManager;

    public Persistence(EntityManagerFactory entityManagerFactory) {
    	entityManager = entityManagerFactory.createEntityManager();
    }

	@SuppressWarnings("unchecked")
	public List<Note> getNotes() {
		List<Note> entities = null;
		try {
			Query jpaQuery = entityManager.createQuery("SELECT n FROM note n");
			entities = jpaQuery.getResultList();
		} catch(Exception e) {
			logger.error("Error while loading entities: ", e);
		}
		return entities;
    }

	public Note getNote(Long id) {
		Note note = null;
		try {
			note = entityManager.find(Note.class, id);
		} catch(Exception e) {
			logger.error("Error while loading entity [" + id + "]: ", e);
		}
		return note;
    }	

	public Note removeNote(Long id) {
		Note note = null;
		try {
    		entityManager.getTransaction().begin();
			note = entityManager.find(Note.class, id);
			entityManager.remove(note);
    		entityManager.getTransaction().commit();    		
		} catch(Exception e) {
			logger.error("Error while removing entity [" + id + "]: ", e);
		}
		return note;
    }
	
	public Note insertNote(Note note) {
		try {
    		entityManager.getTransaction().begin();
			//note = entityManager.find(Note.class, id);
			entityManager.persist(note);
    		entityManager.getTransaction().commit();    		
		} catch(Exception e) {
			logger.error("Error while removing entity [" + note.getNoteID() + "]: ", e);
		}
		return note;
    }

	public Author getAuthor(String id) {
		Author author = null;
		try {
			author = entityManager.find(Author.class, id);
		} catch(Exception e) {
			logger.error("Error while loading entity [" + id + "]: ", e);
		}
		return author;
    }	
}
