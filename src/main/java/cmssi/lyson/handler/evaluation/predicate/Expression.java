/*
 * MIT License
 *
 * Copyright (c) 2019 - 2022  Christophe Munilla
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cmssi.lyson.handler.evaluation.predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import cmssi.lyson.handler.evaluation.EvaluationContext;
import cmssi.lyson.handler.evaluation.predicate.Predicate.ValidationTime;

/**
 * An Expression gathers {@link Predicate}s linked by a {@link LogicalOperator}
 * 
 * @author cmunilla@cmssi.fr
 * @version 0.6
 */
public class Expression implements Verifiable {
	
	protected boolean verified;
	protected boolean verification;
	
	protected LogicalOperator logicalOperator;	
	protected List<Verifiable> verifiables;

	/**
	 * Constructor
	 * 
	 * Instantiates a new Expression 
	 *   
	 * @param logicalOperator the {@link LogicalOperator} of the Expression
	 * to be instantiated
	 * @param verifiables the initial set of {@link Verifiable}s held by the 
	 * Expression to be instantiated
	 */
	public Expression(LogicalOperator logicalOperator, List<Verifiable> verifiables){
		this(logicalOperator);
		if(verifiables!=null) {
			for(Verifiable verifiable:verifiables) {
				addVerifiable(verifiable);
			}
		}
	}
	
	/**
	 * Constructor
	 * 
	 * Instantiates a new Expression 
	 *   
	 * @param logicalOperator the {@link LogicalOperator} of the Expression
	 * to be instantiated
	 */
	public Expression(LogicalOperator logicalOperator){
		this.logicalOperator = logicalOperator==null?LogicalOperator.NOP:logicalOperator;
		this.verifiables = new ArrayList<>();
	}
	
	/**
	 * Adds the {@link Verifiable} passed as parameter to this Expression.<br/>
	 * 
	 * The {@link Verifiable} is effectively added if the set of registered {@link Verifiable}s is  empty or 
	 * if this Expression's {@link LogicalOperator} is not the 'NOT' one.   
	 * 
	 * @param verifiable the {@link Verifiable} to add
	 * 
	 * @return true if the specified {@link Verifiable} has been added; false otherwise 
	 */
	public boolean addVerifiable(Verifiable verifiable) {
		if(verifiable != null && (!LogicalOperator.NOT.equals(this.logicalOperator)||this.verifiables.isEmpty())) {
			this.verifiables.add(verifiable);
			return true;
		}
		return false;
	}

	@Override
	public void verify(EvaluationContext evaluationContext) {
		this.verifiables.forEach(v -> v.verify(evaluationContext));
		this.verification = false;
	}
	
	@Override
	public boolean verified(ValidationTime validationTime) {
		
		if(this.verification)
			return this.verified;
		
		final LogicalOperator lo = this.logicalOperator==null
				?LogicalOperator.AND:this.logicalOperator;
		
		Optional<Boolean> verified = (this.verifiables==null||this.verifiables.isEmpty())?
			Optional.of(true):this.verifiables.stream().map( 
				p -> p.verified(validationTime)).reduce((b1,b2) -> {
					switch(lo) {
						case AND:
							return b1 && b2;
						case OR:
							return b1 || b2;
						case XOR:
							return (b1 && !b2) || (!b1 && b2);
						case NOP:
						case NOT:
						default:
							return b1;
					}
				}
			);
		
		if(verified.isEmpty())
			this.verified = true;
		else 
			this.verified = this.logicalOperator.equals(LogicalOperator.NOT)
			    ?!verified.get().booleanValue():verified.get().booleanValue();
		
		this.verification = true;
		return this.verified;
	}	
}
