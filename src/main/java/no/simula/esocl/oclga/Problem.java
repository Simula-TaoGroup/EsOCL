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

package no.simula.esocl.oclga;

import no.simula.esocl.ocl.distance.ValueElement4Search;
import org.dresdenocl.modelinstance.IModelInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Shaukat Ali
 * @version 1.0
 * @since 2017-07-03
 */
public interface Problem {

    public static final int NUMERICAL_TYPE = 0;
    public static final int CATEGORICAL_TYPE = 1;

    //get the constraints need to solve
    ValueElement4Search[] getConstraintElements4Search();

    //get the Gene Constraints used for search
    ArrayList<GeneValueScope> getGeneConstraints();

    //decoding the individual to the String[]
    String[] decoding(int v[]);

    // list of variables in String[]
    double getFitness(String[] value);


    List<String> getStringInstances();

    List<IModelInstance> getObjects();


}
