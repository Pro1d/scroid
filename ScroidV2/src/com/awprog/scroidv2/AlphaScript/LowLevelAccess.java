package com.awprog.scroidv2.AlphaScript;

import com.awprog.scroidv2.AlphaScript.ErrorDialog.ScriptException;


public interface LowLevelAccess {
	/***** Fonctions accessible depuis les instructions *****/
	
	/**** Context & Cie ****/
	/** Change le compteur ordinal :P */
	public void setNextLine(int line);
	
	/** Change de contexte (appel de fonction avec param�tres) */
	public void pushContext(String file, int line, Data leftParam, Data rightParam) throws ScriptException;
	/** Donne les param�tres re�us par la fonction actuelle */
	public Data getParameterLeft() throws ScriptException;
	public Data getParameterRight() throws ScriptException;
	
	/** Pour sortir d'une fonction */
	public void popContext();
	/** Pour sortir d'une fonction et retourner une valeur */
	public void popContext(Data returnedData);

	/** Interrompre d�finitivement l'ex�cution **/
	public void end();
	
	/**** Acc�s variable ****/
 	/** Cr�e une nouvelle variable (si in�xistante) dans le contexte actuelle et la retourne 
	 * @throws ScriptException */
	public Data createVar(String name, int type) throws ScriptException;
	/** Indique si la variable existe dans ce contexte */
	public boolean isExistingVar(String name);
	/** Donne la r�f�rence de la variable */
	public Data getVar(String name);
	
	/**** Temps ****/
	public void sleep(long millis);
	/** Retourne le nombre de secondes �coul�es depuis le d�but de l'ex�cution**/
	public double getElapsedTime();
	
	/**** Entr�e / Sortie standard ****/
	enum CommandOut { CLEAR, WRITE };
	enum CommandIn { STRING, NUMBER, PAUSE, BOOLEAN };
	/** Pour des commandes sur la sortie standard (console) **/
	public void out(CommandOut cmd, StringBuilder data);
	/** Pour des commandes sur l'entr�e standard (EdtiText + bouton) **/
	public void in(CommandIn cmd, Data data);

}
