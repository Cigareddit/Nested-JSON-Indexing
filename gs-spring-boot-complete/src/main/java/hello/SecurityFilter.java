package hello;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class SecurityFilter implements ContainerRequestFilter {
	
	
	private static final String AUTHORIZATION_HEADER_KEY = "Authorization";
	private static final String AUTHORIZATION_HEADER_PREFIX = "Basic ";
	private static final String SECURED_URI_PREFIX = "display";
	
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		
		if(requestContext.getUriInfo().getPath().contains(SECURED_URI_PREFIX)){
				
			List<String> authHeader = requestContext.getHeaders().get(AUTHORIZATION_HEADER_KEY);
			if(authHeader != null && authHeader.size()>0)
			{
				//check if the authHeader is not null and size is greater than 0
			}
			
			//aborting with the unauthorized message if the credentials are incorrect
			Response unauthorizedStatus  = Response.status(Response.Status.UNAUTHORIZED).entity("User cannot access this device").build();
			requestContext.abortWith(unauthorizedStatus);
			// TODO Auto-generated method stub
		}
		
	}
	

}
