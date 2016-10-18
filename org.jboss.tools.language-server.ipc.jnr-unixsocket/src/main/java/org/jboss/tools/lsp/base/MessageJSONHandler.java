package org.jboss.tools.lsp.base;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.jboss.tools.lsp.base.ResponseError.ReservedCode;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class MessageJSONHandler implements JsonSerializer<Message>, JsonDeserializer<Message>{

	@Override
	public Message deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context)
			throws JsonParseException {
		JsonObject object = jsonElement.getAsJsonObject();
		Type subType = ResponseMessage.class;
		Type paramType = null;
		if(!object.has("id")){
			subType = NotificationMessage.class;
			//TODO: Resolve result type from id. 
		}
		else if(object.has("method")){
			subType = RequestMessage.class;
		}
		String method = object.get("method").getAsString();
		LSPMethods lspm = LSPMethods.fromMethod(method);
		if(lspm == null){
			throw new LSPException(ReservedCode.METHOD_NOT_FOUND.code(), method + " is not handled ", null, null);
		}
		paramType = LSPMethods.fromMethod(method).getRequestType();
		return context.deserialize(jsonElement, new ParameterizedTypeImpl(subType,paramType));
	}

	@Override
	public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
		Type rawType = null; 
		Type paramType = Object.class;
		if(message instanceof NotificationMessage){
			rawType = NotificationMessage.class;
			NotificationMessage<?> nm = (NotificationMessage<?>)message;
			if(nm.getParams() != null){
				paramType = nm.getParams().getClass();
			}
		}
		if(message instanceof ResponseMessage){
			rawType = ResponseMessage.class;
			ResponseMessage<?> rm = (ResponseMessage<?>)message;
			if(rm.getResult() != null ){
				paramType = rm.getResult().getClass();
			}
		}
		if(message instanceof RequestMessage){
			rawType = RequestMessage.class;
			RequestMessage<?>rqm = (RequestMessage<?>)message;
			if(rqm.getParams() != null){
				paramType = rqm.getParams().getClass();
			}
		}
		if(rawType == null)
			throw new RuntimeException("Unrecognized message type");
		return context.serialize(message,new ParameterizedTypeImpl(rawType,paramType));
	}
	
	static class ParameterizedTypeImpl implements ParameterizedType{

		final private Type rawType;
		final private Type paramType;
		
		public ParameterizedTypeImpl(Type rawType, Type paramType) {
			this.rawType = rawType;
			this.paramType = paramType;
		}
		
		@Override
		public Type[] getActualTypeArguments() {
			return new Type[]{paramType};
		}

		@Override
		public Type getRawType() {
			return rawType;
		}

		@Override
		public Type getOwnerType() {
			return null;
		}
		
	}
	
}