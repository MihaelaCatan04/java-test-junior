package com.java.test.junior;

import com.java.test.junior.controller.AuthIT;
import com.java.test.junior.controller.InteractionIT;
import com.java.test.junior.controller.ProductIT;
import com.java.test.junior.controller.LoadingIT;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        AuthIT.class,
        ProductIT.class,
        InteractionIT.class,
        LoadingIT.class
})
public class IntegrationTestSuite {
}