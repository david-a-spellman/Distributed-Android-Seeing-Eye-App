package com.example.myapplicationandroidseeingeyeapplication.axmlrpc.serializer;

import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.XMLRPCException;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 *
 * @author Tim Roes
 */
public class NullSerializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		return null;
	}

	public XmlElement serialize(Object object) {
		return new XmlElement(SerializerHandler.TYPE_NULL);
	}

}