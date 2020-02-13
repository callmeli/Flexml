package com.guet.flexbox.el;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        ELProcessor elProcessor = new ELProcessor();
        elProcessor.defineBean("test",new TestBean());
        LambdaExpression lambdaExpression;
        System.out.println(lambdaExpression= (LambdaExpression) elProcessor.eval("(a)->{()->{System.out.println('121212')}}"));
        lambdaExpression.invoke(this);
        assertEquals(4, 2 + 2);
    }
}

