package com.groundworkopensource.webapp.console;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;

public class ManagedBeanFactory {

	/**
	 * Gets the managed bean instance
	 * 
	 * @param beanName
	 * @return
	 */
	public static Object getManagedBean(String beanName) {
		Object resultObj = null;
		if (beanName != null) {

			FacesContext facesContext = FacesContext.getCurrentInstance();
			Application app = facesContext.getApplication();
			ExpressionFactory ef = app.getExpressionFactory();
			ValueExpression valExp = ef.createValueExpression(FacesContext
					.getCurrentInstance().getELContext(),
					"#{" + beanName + "}", Object.class);
			resultObj = valExp.getValue(facesContext.getELContext());
		}

		return resultObj;
	}

}
