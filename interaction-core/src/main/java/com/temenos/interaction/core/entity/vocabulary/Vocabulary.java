package com.temenos.interaction.core.entity.vocabulary;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A Vocabulary contains a set of Terms used to describe resources.
 */
public class Vocabulary {
	//Map of <Term name, Term>
	private Map<String, Term> terms = new HashMap<String, Term>();
	
	/**
	 * Returns the specified Term
	 * @param termName Term name
	 * @return Term
	 */
	public Term getTerm(String termName) {
		return terms.get(termName);
	}
	
	/**
	 * Adds the specified Term to the Vocabulary
	 * @param term Term
	 */
	public void setTerm(Term term) {
		terms.put(term.getName(), term);
	}

	/**
	 * Returns the terms contained in this vocabulary
	 * @return list of terms
	 */
	public Collection<Term> getTerms() {
		return terms.values();
	}
}
