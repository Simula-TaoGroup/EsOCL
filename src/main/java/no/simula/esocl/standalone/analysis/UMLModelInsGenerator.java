/* ****************************************************************************
 * Copyright (c) 2017 Simula Research Laboratory AS.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Shaukat Ali  shaukat@simula.no
 **************************************************************************** */

package no.simula.esocl.standalone.analysis;

import no.simula.esocl.ocl.distance.ValueElement4Search;
import no.simula.esocl.standalone.modelinstance.UMLAttributeIns;
import no.simula.esocl.standalone.modelinstance.UMLObjectIns;
import org.apache.log4j.Logger;
import org.dresdenocl.essentialocl.types.CollectionType;
import org.dresdenocl.metamodels.uml2.internal.model.UML2Class;
import org.dresdenocl.metamodels.uml2.internal.model.UML2Enumeration;
import org.dresdenocl.metamodels.uml2.internal.model.UML2PrimitiveType;
import org.dresdenocl.pivotmodel.Constraint;
import org.dresdenocl.pivotmodel.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Shaukat Ali
 * @version 1.0
 * @since 2017-07-03
 */
public class UMLModelInsGenerator {
    public static int i = 0;
    static Logger logger = Logger.getLogger(UMLModelInsGenerator.class);
    /**
     * this list is initialized for recording the attribute information after confirming the
     * instance number
     */
    private List<UMLAttributeIns> attributeInsList = new ArrayList<>();
    /**
     * this list contains the class instance
     */
    private List<UMLObjectIns> umlObjectInsList;

    private VESGenerator vesGenerator;
    private StringBuilder solution = new StringBuilder();

    public UMLModelInsGenerator(VESGenerator vesGenerator) {
        this.vesGenerator = vesGenerator;
        this.umlObjectInsList = new ArrayList<>();

    }

    public List<UMLObjectIns> getUmlObjectInsList() {
        return umlObjectInsList;
    }


    /**
     * reassign the value of each attribute
     *
     * @param solutions the search engine transfer the value string to calculate the fitness
     */
    public void reAssignedUMLObjects(String[] solutions) {
        solution = new StringBuilder();
        // the order of value is consistent with the attributeInsList
        logger.debug("******************* Building the class Instance (" + i++ + ")*******************");


        for (int i = 0; i < this.attributeInsList.size(); i++) {

            Type type = this.attributeInsList.get(i).getType();

            if (type instanceof UML2PrimitiveType) {

                String typeValue = ((UML2PrimitiveType) type).getKind()
                        .getName();

                switch (typeValue) {
                    case "Integer":
                        this.attributeInsList.get(i).setValue(
                                "" + Double.valueOf(solutions[i]).intValue());
                        break;
                    case "Boolean":
                        double temp = Double.valueOf(solutions[i]);
                        if ((temp - 1.0) == 0) {
                            this.attributeInsList.get(i).setValue("true");
                        } else {
                            this.attributeInsList.get(i).setValue("false");
                        }
                        break;
                    case "String":
                        this.attributeInsList.get(i).setValue(solutions[i]);
                        break;
                    case "Real":
                        this.attributeInsList.get(i).setValue(solutions[i]);
                        break;
                }


            } else if (type instanceof UML2Enumeration) {
                UML2Enumeration enumType = (UML2Enumeration) type;

                String literalName = enumType.getOwnedLiteral()

                        .get(Double.valueOf(solutions[i]).intValue()).getName();

                this.attributeInsList.get(i).setValue(literalName);
            }

            solution.append("Attribute: ");
            solution.append(this.attributeInsList.get(i).getQualifiedName());
            solution.append(" = ");
            solution.append(this.attributeInsList.get(i).getValue());
            solution.append(" , ");

            logger.debug("Attribute name: "
                    + this.attributeInsList.get(i).getName()
                    + "  --------  Assigned Value: " + this.attributeInsList.get(i).getValue());
        }

    }

    public ValueElement4Search[] getVes4InsNumberArray() {
        // this list is build with the number of instance value
        List<ValueElement4Search> ves4InsNumberList = new ArrayList<ValueElement4Search>();
        Constraint constraint = this.vesGenerator.getConstraint();
        UML2Class contextCLass = (UML2Class) constraint.getConstrainedElement()
                .get(0);
        String contextClassName = contextCLass.getQualifiedName();
        List<ValueElement4Search> vesList = this.vesGenerator
                .getIniVesGroupByClassMap().get(contextClassName);
        UMLObjectIns uoi = buildUMLObjectFromVesList(vesList, contextClassName);
        logger.debug("Build the class instance:: ClassName= "
                + uoi.getQualifiedName() + " Attrs: " + uoi.getAttributeNames());
        this.umlObjectInsList.add(uoi);
        logger.debug("*******************Generate the ves array from the this.umlObjectInsList*******************");
        for (UMLObjectIns umlObject : this.umlObjectInsList) {
            List<ValueElement4Search> initialVes4SameClassList = this.vesGenerator
                    .getVesList4Class(umlObject.getQualifiedName());
            if (initialVes4SameClassList != null) {
                Collection<UMLAttributeIns> nonAssAttrs = umlObject
                        .getPrimitivePropertyCollection();
                this.attributeInsList.addAll(nonAssAttrs);
                for (UMLAttributeIns unapi : nonAssAttrs) {
                    ValueElement4Search initialVes = this.vesGenerator.getVes(
                            initialVes4SameClassList, unapi.getQualifiedName());
                    ValueElement4Search newVes = initialVes.createNewInstance();
                    ves4InsNumberList.add(newVes);
                }
            }
        }
        ValueElement4Search[] ves4InsNumberArray = new ValueElement4Search[ves4InsNumberList
                .size()];
        ves4InsNumberArray = ves4InsNumberList.toArray(ves4InsNumberArray);
        logger.debug("Generate the number of nonAss Ves: "
                + ves4InsNumberArray.length + "/n");
        return ves4InsNumberArray;
    }

    /**
     * to generate the UMLObjectIns from the ves4ClassList based on the association value it is the
     * recursive process
     *
     * @return UMLObjectIns
     */

    public UMLObjectIns buildUMLObjectFromVesList(
            List<ValueElement4Search> initialVes4SameClassList, String className) {
        if (initialVes4SameClassList == null)
            return null;
        UMLObjectIns uoi = new UMLObjectIns(className, null);
        for (ValueElement4Search ves : initialVes4SameClassList) {
            // this ves is the attribute of class
            if (ves.getSourceClass().equals(ves.getDestinationClass())) {
                UMLAttributeIns attrIns = new UMLAttributeIns(
                        ves.getAttributeName(), null);
                Type attrType = ves.getProperty().getType();
                if (attrType instanceof CollectionType) {
                    List<UMLAttributeIns> attrInsList = new ArrayList<UMLAttributeIns>();
                    attrIns.setType(((CollectionType) attrType)
                            .getElementType());
                    attrInsList.add(attrIns);
                    for (int i = 1; i < ves.getMaxValue(); i++) {
                        UMLAttributeIns temp_attrIns = new UMLAttributeIns(
                                ves.getAttributeName(), null);
                        temp_attrIns.setType(((CollectionType) attrType)
                                .getElementType());
                        attrInsList.add(temp_attrIns);
                    }
                    uoi.addProperty(ves.getAttributeName(), attrInsList);// luhong relate instances together
                } else {
                    attrIns.setType(attrType);
                    uoi.addProperty(ves.getAttributeName(), attrIns);
                }

            } else {
                /*
                 * handle the local association property with out overall analysis e.g. we only
                 * analyze the situation like A--*B---*C
                 */

                List<UMLObjectIns> assUoiList = new ArrayList<UMLObjectIns>();
                int numberOfAssClassIns = 0;
                if (ves.getValue() != null) {
                    numberOfAssClassIns = Integer.valueOf(ves.getValue());
                }
                List<ValueElement4Search> assVes4SameClassList = this.vesGenerator
                        .getVesList4Class(ves.getDestinationClass());
                for (int i = 0; i < numberOfAssClassIns; i++) {
                    UMLObjectIns assUoi = null;
                    if (assVes4SameClassList == null) {
                        assUoi = new UMLObjectIns(ves.getDestinationClass(),
                                null);
                    } else {
                        assUoi = buildUMLObjectFromVesList(
                                assVes4SameClassList, ves.getDestinationClass());
                    }
                    if (assUoi != null) {
                        logger.debug("Build the class instance:: ClassName= "
                                + assUoi.getQualifiedName() + " Attrs: "
                                + assUoi.getAttributeNames() + "/n");
                        this.umlObjectInsList.add(assUoi);
                        assUoiList.add(assUoi);
                    }
                }
                // assUoiList.addAll(getUMLObjects(ves.getDestinationClass()));
                uoi.addProperty(ves.getAttributeName(), assUoiList);
            }
        }
        return uoi;
    }

    /**
     * find the list of UMLObjectIns from the this.umlObjectInsList based on the class name
     *
     * @param className
     * @return
     */
    public List<UMLObjectIns> getUMLObjects(String className) {
        List<UMLObjectIns> identifiedObjects = new ArrayList<UMLObjectIns>();
        for (UMLObjectIns uoi : this.umlObjectInsList) {
            if (uoi.getQualifiedName().equals(className))
                identifiedObjects.add(uoi);
        }
        return identifiedObjects;
    }

    public String getSolution() {
        return solution.toString();
    }
}
