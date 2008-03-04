package org.genedb.jogra.drawing;

import org.jdesktop.swingx.auth.LoginService;

public class JograLoginService extends LoginService {

	@Override
	public boolean authenticate(String uname, char[] passwd, String server)
			throws Exception {
		String password = new String(passwd);
		System.err.println("Trying to log in as '"+password+"'");
		// TODO Auto-generated method stub
		if (password.equals("fred")) {
			return true;
		}
		return false;
	}

}
