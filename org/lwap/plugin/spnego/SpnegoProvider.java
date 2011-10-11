package org.lwap.plugin.spnego;

import java.io.IOException;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;

import org.lwap.plugin.spnego.SpnegoConfig.Constants;

public class SpnegoProvider {	

    /** Factory for GSS-API mechanism. */
    static final GSSManager MANAGER = GSSManager.getInstance(); // NOPMD

    /** GSS-API mechanism "1.3.6.1.5.5.2". */
    static final Oid SPNEGO_OID = SpnegoProvider.getOid(); // NOPMD

    /*
     * This is a utility class (not a Singleton).
     */
    private SpnegoProvider() {
        // default private
    }

    static SpnegoAuthScheme negotiate(
        final HttpServletRequest req, final SpnegoHttpServletResponse resp
        , final boolean basicSupported, final boolean promptIfNtlm
        , final String realm) throws IOException {

        final SpnegoAuthScheme scheme = SpnegoProvider.getAuthScheme(
                req.getHeader(Constants.AUTHZ_HEADER));
        
        if (null == scheme || scheme.getToken().length == 0) {
//            LOGGER.finer("Header Token was NULL");
            resp.setHeader(Constants.AUTHN_HEADER, Constants.NEGOTIATE_HEADER);
//            resp.setHeader(Constants.AUTHN_HEADER, "Kerberos");
            
            if (basicSupported) {
                resp.addHeader(Constants.AUTHN_HEADER,
                    Constants.BASIC_HEADER + " realm=\"" + realm + '\"');
            } else {
//                LOGGER.finer("Basic NOT offered: Not Enabled or SSL Required.");
            }

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED, true);
            
            return null;
            
        }
        
        // assert
        if (scheme.isNtlmToken()) {
//            LOGGER.warning("Downgrade NTLM request to Basic Auth.");

            if (resp.isStatusSet()) {
                throw new IllegalStateException("HTTP Status already set.");
            }

            if (basicSupported && promptIfNtlm) {
                resp.setHeader(Constants.AUTHN_HEADER,
                        Constants.BASIC_HEADER + " realm=\"" + realm + '\"');
            } else {
                // TODO : decode/decrypt NTLM token and return a new SpnegoAuthScheme
                // of type "Basic" where the token value is a base64 encoded
                // username + ":" + password string
                throw new UnsupportedOperationException("NTLM specified. Downgraded to " 
                        + "Basic Auth (and/or SSL) but downgrade not supported.");
            }
            
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED, true);
            
            return null;
        }
        
        return scheme;
    }
    
    /**
     * Returns the GSS-API interface for creating a security context.
     * 
     * @param subject the person to be authenticated
     * @return GSSCredential to be used for creating a security context.
     * @throws PrivilegedActionException
     */
    public static GSSCredential getClientCredential(final Subject subject)
        throws PrivilegedActionException {

        final PrivilegedExceptionAction<GSSCredential> action = 
            new PrivilegedExceptionAction<GSSCredential>() {
                public GSSCredential run() throws GSSException {
                    return MANAGER.createCredential(
                        null
                        , GSSCredential.DEFAULT_LIFETIME
                        , SpnegoProvider.SPNEGO_OID
                        , GSSCredential.INITIATE_ONLY);
                } 
            };
        
        return Subject.doAs(subject, action);
    }
    
    /**
     * Returns a GSSContext to be used by custom clients to set 
     * data integrity requirements, confidentiality and if mutual 
     * authentication is required.
     * 
     * @param creds credentials of the person to be authenticated
     * @param url HTTP address of server (used for constructing a {@link GSSName}).
     * @return GSSContext 
     * @throws GSSException
     * @throws PrivilegedActionException
     */
    public static GSSContext getGSSContext(final GSSCredential creds, final URL url) 
        throws GSSException {
        
        return MANAGER.createContext(SpnegoProvider.getServerName(url)
                , SpnegoProvider.SPNEGO_OID
                , creds
                , GSSContext.DEFAULT_LIFETIME);
    }
    
    /**
     * Returns the {@link SpnegoAuthScheme} or null if header is missing.
     * 
     * <p>
     * Throws UnsupportedOperationException if header is NOT Negotiate 
     * or Basic. 
     * </p>
     * 
     * @param header ex. Negotiate or Basic
     * @return null if header missing/null else the auth scheme
     */
    public static SpnegoAuthScheme getAuthScheme(final String header) {

        if (null == header || header.isEmpty()) {
//            LOGGER.finer("authorization header was missing/null");
            return null;
            
        } else if (header.startsWith(Constants.NEGOTIATE_HEADER)) {
            final String token = header.substring(Constants.NEGOTIATE_HEADER.length() + 1);
            return new SpnegoAuthScheme(Constants.NEGOTIATE_HEADER, token);
            
        } else if (header.startsWith(Constants.BASIC_HEADER)) {
            final String token = header.substring(Constants.BASIC_HEADER.length() + 1);
            return new SpnegoAuthScheme(Constants.BASIC_HEADER, token);
            
        } else {
            throw new UnsupportedOperationException("Negotiate or Basic Only:" + header);
        }
    }
    
    /**
     * Returns the Universal Object Identifier representation of 
     * the SPNEGO mechanism.
     * 
     * @return Object Identifier of the GSS-API mechanism
     */
    private static Oid getOid() {
        Oid oid = null;
        try {
            oid = new Oid("1.3.6.1.5.5.2");
        } catch (GSSException gsse) {
//            LOGGER.log(Level.SEVERE, "Unable to create OID 1.3.6.1.5.5.2 !", gsse);
        }
        return oid;
    }

    /**
     * Returns the {@link GSSCredential} the server uses for pre-authentication.
     * 
     * @param subject account server uses for pre-authentication
     * @return credential that allows server to authenticate clients
     * @throws PrivilegedActionException
     */
    static GSSCredential getServerCredential(final Subject subject)
        throws PrivilegedActionException {
        
        final PrivilegedExceptionAction<GSSCredential> action = 
            new PrivilegedExceptionAction<GSSCredential>() {
                public GSSCredential run() throws GSSException {
                    return MANAGER.createCredential(
                        null
                        , GSSCredential.INDEFINITE_LIFETIME
                        , SpnegoProvider.SPNEGO_OID
                        , GSSCredential.ACCEPT_ONLY);
                } 
            };
        return Subject.doAs(subject, action);
    }

    /**
     * Returns the {@link GSSName} constructed out of the passed-in 
     * URL object.
     * 
     * @param url HTTP address of server
     * @return GSSName of URL.
     * @throws GSSException 
     */
    static GSSName getServerName(final URL url) throws GSSException {
        return MANAGER.createName("HTTP@" + url.getHost(),
            GSSName.NT_HOSTBASED_SERVICE, SpnegoProvider.SPNEGO_OID);
    }

    /**
     * Used by the BASIC Auth mechanism for establishing a LoginContext 
     * to authenticate a client/caller/request.
     * 
     * @param username client username
     * @param password client password
     * @return CallbackHandler to be used for establishing a LoginContext
     */
    public static CallbackHandler getUsernamePasswordHandler(
        final String username, final String password) {

//        LOGGER.fine("username=" + username + "; password=" + password.hashCode());

        final CallbackHandler handler = new CallbackHandler() {
            public void handle(final Callback[] callback) {
                for (int i=0; i<callback.length; i++) {
                    if (callback[i] instanceof NameCallback) {
                        final NameCallback nameCallback = (NameCallback) callback[i];
                        nameCallback.setName(username);
                    } else if (callback[i] instanceof PasswordCallback) {
                        final PasswordCallback passCallback = (PasswordCallback) callback[i];
                        passCallback.setPassword(password.toCharArray());
                    } else {
//                        LOGGER.warning("Unsupported Callback i=" + i + "; class=" 
//                                + callback[i].getClass().getName());
                    }
                }
            }
        };

        return handler;
    }
}
