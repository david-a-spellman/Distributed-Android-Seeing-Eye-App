package com.example.myapplicationandroidseeingeyeapplication.axmlrpc.serializer;

import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.XMLRPCException;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.XMLUtil;
import com.example.myapplicationandroidseeingeyeapplication.axmlrpc.xmlcreator.XmlElement;
import java.util.*;
import org.w3c.dom.Element;

/**
 *
 * @author Tim Roes
 */
public class Base64Serializer implements Serializer {

	public Object deserialize(Element content) throws XMLRPCException {
		//return Base64.decode(XMLUtil.getOnlyTextContent(content.getChildNodes()));
		return null;
	}

	public XmlElement serialize(Object object) {
		//return XMLUtil.makeXmlTag(SerializerHandler.TYPE_BASE64,
		//		Base64.encode((Byte[])object));
		return null;
	}

}