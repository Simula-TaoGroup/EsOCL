package simula.standalone.modelinstance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class UMLObjectInstance extends AbstractUMLModelInstance {

	public UMLObjectInstance(String name, String value) {
		super(name, value);
		// TODO Auto-generated constructor stub
	}

	private Map<String, Object> propertyMap = new HashMap<String, Object>();

	public Object getPropertyObject(String propertyName) {
		return propertyMap.get(propertyName);
	}

	public void addProperty(String propertyName, Object umlProperty) {
		propertyMap.put(propertyName, umlProperty);
	}
	
	public Collection<UMLPrimitivePropertyInstance> getPrimitivePropertyCollection(){
		Collection<Object> properties = propertyMap.values();
		Collection<UMLPrimitivePropertyInstance> primitiveProperties = new HashSet<UMLPrimitivePropertyInstance>();
		for (Object object : properties) {
			if (object instanceof UMLPrimitivePropertyInstance){
				primitiveProperties.add((UMLPrimitivePropertyInstance)object);
			}
		}
		return primitiveProperties;
	}

}
