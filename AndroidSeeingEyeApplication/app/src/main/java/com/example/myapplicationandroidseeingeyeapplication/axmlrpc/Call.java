package com.example.myapplicationandroidseeingeyeapplication.axmlrpc;

import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.serializer.Serializer;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.serializer.SerializerHandler;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.xmlcreator.SimpleXMLCreator;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.xmlcreator.XmlElement;

/**
 * A Call object represents a call of a remote methode.
 * It contains the name of the method to be called and the parameters to use
 * in this remote procedure call. To send it over the network the method getXML
 * returns an xml representation according to the XML-RPC specification as a String.
 *
 * @author Tim Roes
 */
public class Call {

	private String method;
	private Object[] params;
	private final SerializerHandler serializerHandler;

	/**
	 * Create a new method call with the given name and no parameters.
	 * @param serializerHandler You can inject an arbitrary one if you want to use your own transport protocol.
	 *     See the README (section "Using an arbitrary transport") for more info on this feature.
	 * @param method The method to be called.
	 */
	public Call(SerializerHandler serializerHandler, String method) {
		this(serializerHandler, method, null);
	}

	/**
	 * Create a new method call with the given name and parameters.
	 * @param serializerHandler You can inject an arbitrary one if you want to use your own transport protocol.
	 *     See the README (section "Using an arbitrary transport") for more info on this feature.
	 * @param method The method to be called.
	 * @param params An array of parameters for the method.
	 */
	public Call(SerializerHandler serializerHandler, String method, Object[] params) {
		this.method = method;
		this.params = params;
		this.serializerHandler = serializerHandler;
	}

	/**
	 * Return an xml representation of the method call as specified in
	 * http://www.xmlrpc.com/spec. If flags have been set in the XMLRPCClient
	 * the returning xml does not comply strict to the standard.
	 *
	 * @param debugMode This prints data on System.out to make it easy to debug
	 *
	 * @return The string of the xml representing this call.
	 * @throws XMLRPCException Will be thrown whenever the xml representation cannot
	 * 		be build without errors.
	 * @see XMLRPCClient
	 */
	public String getXML(boolean debugMode) throws XMLRPCException {

		SimpleXMLCreator creator = new SimpleXMLCreator();

		XmlElement methodCall = new XmlElement(XMLRPCClient.METHOD_CALL);
		creator.setRootElement(methodCall);

		XmlElement methodName = new XmlElement(XMLRPCClient.METHOD_NAME);
		methodName.setContent(method);
		methodCall.addChildren(methodName);

		if(params != null && params.length > 0) {
			XmlElement callParams = new XmlElement(XMLRPCClient.PARAMS);
			methodCall.addChildren(callParams);

			for(Object o : this.params) {
				callParams.addChildren(getXMLParam(o));
			}
		}

		String result = creator.toString();

		if ( debugMode){
			System.out.println(result);
		}

		return result;
	}

	/**
	 * Generates the param xml tag for a specific parameter object.
	 *
	 * @param o The parameter object.
	 * @return The object serialized into an xml tag.
	 * @throws XMLRPCException Will be thrown if the serialization failed.
	 */
	private XmlElement getXMLParam(Object o) throws XMLRPCException {
		XmlElement param = new XmlElement(XMLRPCClient.PARAM);
		XmlElement value = new XmlElement(XMLRPCClient.VALUE);
		param.addChildren(value);
		value.addChildren(serializerHandler.serialize(o));
		return param;
	}

}
