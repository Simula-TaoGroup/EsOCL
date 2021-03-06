package simula.standalone.analysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;

import tudresden.ocl20.pivot.essentialocl.expressions.OclExpression;
import tudresden.ocl20.pivot.essentialocl.expressions.Variable;
import tudresden.ocl20.pivot.essentialocl.expressions.impl.IteratorExpImpl;
import tudresden.ocl20.pivot.essentialocl.expressions.impl.OperationCallExpImpl;
import tudresden.ocl20.pivot.essentialocl.standardlibrary.OclAny;
import tudresden.ocl20.pivot.essentialocl.standardlibrary.OclBoolean;
import tudresden.ocl20.pivot.interpreter.internal.OclInterpreter;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceElement;
import tudresden.ocl20.pivot.modelinstancetype.types.IModelInstanceObject;

public class BDC4IterateOp {
	private Utility utility = Utility.INSTANCE;

	OclInterpreter interpreter;

	private OCLExpUtility oclExpUtility = OCLExpUtility.INSTANCE;

	public BDC4IterateOp(OclInterpreter interpreter) {
		this.interpreter = interpreter;
	}

	public double handleIteratorOp(IModelInstanceObject env,
			IteratorExpImpl iteratorExp) {
		this.interpreter.setEnviromentVariable("self", env);
		// obtain the operator name of iteration expression
		String opName = iteratorExp.getName();
		EList<EObject> contents = iteratorExp.eContents();
		// obtain the result elements of left part in the check expression e.g. the self part of
		// self->forAll
		Collection<IModelInstanceElement> envCol = oclExpUtility
				.getResultCollection(this.interpreter.doSwitch(contents.get(0)));
		int envColsize = envCol.size();
		// transform the collection to array
		IModelInstanceElement[] envArray = new IModelInstanceElement[envColsize];
		envArray = envCol.toArray(envArray);
		// obtain the iterators of iteration expression
		List<Variable> iterators = iteratorExp.getIterator();
		String complexType = oclExpUtility.isComplexType(iteratorExp);
		// handle the complex situation e.g. select->forAll
		if (complexType != null
				&& complexType.equals(OCLExpUtility.OP_COMPLEX_SELECT_ITERATE)) {
			return handleComplexSelectIterateOp(env, iteratorExp);
		}
		// argument expression for each iteration
		OclExpression paraExp = (OclExpression) contents.get(1);
		if (opName.equals("forAll")) {
			return forAllOp(env, envArray, iterators, null, paraExp, null);
		} else if (opName.equals("exists")) {
			return existsOp(env, envArray, iterators, null, paraExp, null);
		} else if (opName.equals("isUnique")) {
			return isUniqueOp(envArray, iterators, paraExp);
		} else if (opName.equals("one")) {
			return oneOp(env, envArray, iterators, null, paraExp, null);
		}

		return -1;
	}

	private double handleComplexSelectIterateOp(IModelInstanceObject env,
			IteratorExpImpl iteratorExp) {
		String opName = iteratorExp.getName();
		EObject selectExp = iteratorExp.eContents().get(0);
		// obtain the result elements of left part in the select expression e.g. the self part of
		// self->select->forAll
		Collection<IModelInstanceElement> envCol = oclExpUtility
				.getResultCollection(this.interpreter.doSwitch(selectExp
						.eContents().get(0)));
		IModelInstanceElement[] envArray = new IModelInstanceElement[envCol
				.size()];
		envArray = envCol.toArray(envArray);
		// obtain the iterator of select expression
		Variable selectIterator = ((IteratorExpImpl) selectExp).getIterator()
				.get(0);
		// obtain the iterators of iteration expression
		List<Variable> iterateIterators = iteratorExp.getIterator();
		// obtain the argument expression of select and iteration expression
		OclExpression iterateParaExp = (OclExpression) iteratorExp.eContents()
				.get(1);
		OclExpression selectParaExp = (OclExpression) selectExp.eContents()
				.get(1);
		double distance = -1;
		if (opName.equals("forAll")) {
			distance = forAllOp(env, envArray, iterateIterators,
					selectIterator, iterateParaExp, selectParaExp);
		} else if (opName.equals("exists")) {
			distance = existsOp(env, envArray, iterateIterators,
					selectIterator, iterateParaExp, selectParaExp);
		} else if (opName.equals("isUnique")) {
			distance = isUniqueOp(envArray, iterateIterators, iterateParaExp);
		} else if (opName.equals("one")) {
			distance = oneOp(env, envArray, iterateIterators, selectIterator,
					iterateParaExp, selectParaExp);
		}
		return distance;
	}

	private double forAllOp(IModelInstanceObject env,
			IModelInstanceElement[] envArray, List<Variable> forAllIterators,
			Variable selectIterator, OclExpression forAllParaExp,
			OclExpression selectParaExp) {
		// build the index array of source elements
		int[] input = utility.genIndexArray(envArray.length);
		int iteratorSize = forAllIterators.size();
		if (envArray.length == 0)
			return 0;
		else {
			double temp = 0;
			/**
			 *  build the combination with repetition for index array
			 *  
			 *  input = {0,1}
			 *  e.g. the result is {0,0},{0,1},{1,0},{1,1}
			 */
			
			int[][] envComb = utility.combInArrayDup(input, iteratorSize);
			for (int i = 0; i < envComb.length; i++) {
				
				for (int j = 0; j < iteratorSize; j++) {
					this.interpreter.setEnviromentVariable(
							forAllIterators.get(j).getName(),
							envArray[envComb[i][j]]);
				}
				BDC4BooleanOp bdc4BoolOp = new BDC4BooleanOp(this.interpreter);
				if (selectParaExp == null)
					temp += bdc4BoolOp.handleBooleanOp(env, forAllParaExp);
				else {
					temp = bdc4BoolOp.handleBooleanOp(env, forAllParaExp);
					for (int j = 0; j < forAllIterators.size(); j++) {
						this.interpreter.setEnviromentVariable(
								selectIterator.getName(),
								envArray[envComb[i][j]]);
						temp = utility.normalize(Math.min(temp,
								bdc4BoolOp.notOp(env, selectParaExp)));
					}
				}
			}
			return utility.formatValue(temp / envComb.length);
		}
	}

	private double existsOp(IModelInstanceObject env,
			IModelInstanceElement[] envArray, List<Variable> existsIterators,
			Variable selectIterator, OclExpression existsParaExp,
			OclExpression selectParaExp) {
		int[] input = utility.genIndexArray(envArray.length);
		int existsIteratorSize = existsIterators.size();
		double temp = 0, min_value = 1;
		// Composition with replication
		int[][] envComb = utility.combInArrayDup(input, existsIteratorSize);
		for (int i = 0; i < envComb.length; i++) {
			for (int j = 0; j < existsIteratorSize; j++) {
				this.interpreter.setEnviromentVariable(existsIterators.get(j)
						.getName(), envArray[envComb[i][j]]);
			}
			BDC4BooleanOp bdc4BoolOp = new BDC4BooleanOp(this.interpreter);
			if (selectParaExp == null)
				temp = bdc4BoolOp.handleBooleanOp(env, existsParaExp);
			else {
				// temp = bdc4BoolOp.classifyValue(env, p1, p2, modifyOp);
				temp = bdc4BoolOp.handleBooleanOp(env, existsParaExp);
				for (int j = 0; j < existsIteratorSize; j++) {
					this.interpreter.setEnviromentVariable(
							selectIterator.getName(), envArray[envComb[i][j]]);
					temp = utility.normalize(temp
							+ bdc4BoolOp.handleBooleanOp(env, selectParaExp));
				}
			}

			if (temp - min_value < 0) {
				min_value = temp;
			}
		}
		return min_value;
	}

	public double isUniqueOp(IModelInstanceElement[] envArray,
			List<Variable> uniqueIterators, OclExpression uniqueParaExp) {
		int[] input = utility.genIndexArray(envArray.length);
		double temp = 0;
		int envColsize = envArray.length;
		BDC4CompareOp bdc4CompOp = new BDC4CompareOp(this.interpreter);
		int[][] envComb = Utility.INSTANCE.getArrangeOrCombine(input);
		for (int i = 0; i < envComb.length; i++) {
			this.interpreter.setEnviromentVariable(uniqueIterators.get(0)
					.getName(), envArray[envComb[i][0]]);
			double leftValue = oclExpUtility
					.getResultNumericValue(this.interpreter.doSwitch(
							uniqueParaExp).getModelInstanceElement());
			this.interpreter.setEnviromentVariable(uniqueIterators.get(0)
					.getName(), envArray[envComb[i][1]]);
			double rightValue = oclExpUtility
					.getResultNumericValue(this.interpreter.doSwitch(
							uniqueParaExp).getModelInstanceElement());
			temp += bdc4CompOp.compareOp4Numeric(leftValue, rightValue, "<>");
		}
		return Utility.INSTANCE.formatValue(2 * temp
				/ (envColsize * (envColsize - 1)));

	}

	public double oneOp(IModelInstanceObject env,
			IModelInstanceElement[] envArray, List<Variable> oneIterators,
			Variable selectIterator, OclExpression oneParaExp,
			OclExpression selectParaExp) {
		BDC4BooleanOp bdc4BooleanOp = new BDC4BooleanOp(interpreter);
		int count = 0;
		double temp = 0, temp_not = 0;
		for (int i = 0; i < envArray.length; i++) {
			this.interpreter.setEnviromentVariable(oneIterators.get(0)
					.getName(), envArray[i]);
			if (selectParaExp != null) {
				this.interpreter.setEnviromentVariable(
						selectIterator.getName(), envArray[i]);
				// d(p)
				temp += bdc4BooleanOp.andOp(env, selectParaExp, oneParaExp);
				OclAny oneParaResult = this.interpreter.doSwitch(oneParaExp);
				OclAny selectParaResult = this.interpreter
						.doSwitch(selectParaExp);
				// d(not p)
				if (((OclBoolean) oneParaResult).isTrue()
						&& ((OclBoolean) selectParaResult).isTrue()) {
					temp_not += bdc4BooleanOp.andOp(env, selectParaExp,
							oneParaExp);
				}

				if (((OclBoolean) oneParaResult).isTrue()
						&& ((OclBoolean) selectParaResult).isTrue())
					count++;
			} else {
				// d(p)
				temp += bdc4BooleanOp.handleBooleanOp(env, oneParaExp);
				// d(not p)
				temp_not += bdc4BooleanOp.notOp(env, oneParaExp);
				OclAny oneParaResult = this.interpreter.doSwitch(oneParaExp);
				if (((OclBoolean) oneParaResult).isTrue())
					count++;
			}
		}
		if ((count - 1) > 0)
			return count - 1 + Utility.K + utility.normalize(temp_not);
		else if ((count - 1) < 0)
			return 1 - envArray.length + Utility.K + utility.normalize(temp);
		else
			return 0;
	}

}
