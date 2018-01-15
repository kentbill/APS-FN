package com.esquel.APS.GEW_FN.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("Word")
public class Word extends AbstractPersistable{
	
	private String key;
	private String word_ENG;
	private String word_CHN;
	

	public String getKey() {
		return key;
	}


	public void setKey(String key) {
		this.key = key;
	}

	public String getWord_ENG() {
		return word_ENG;
	}

	public void setWord_ENG(String word_ENG) {
		this.word_ENG = word_ENG;
	}

	public String getWord_CHN() {
		return word_CHN;
	}

	public void setWord_CHN(String word_CHN) {
		this.word_CHN = word_CHN;
	}

	

	public Word(String key, String word_ENG, String word_CHN) {
		super();
		this.key = key;
		this.word_ENG = word_ENG;
		this.word_CHN = word_CHN;
	}


	@Override
	    public String toString() {
	        return getClass().getName().replaceAll(".*\\.", "") + "-" + this.key;
	    }
	  
}
