package org.mobicents.slee.resource.sip11.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

import javax.sip.ListeningPoint;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.ConfigProperties.Property;
import javax.slee.resource.InvalidConfigurationException;
import javax.slee.resource.ResourceAdaptorContext;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.ext.javax.sip.SipStackImpl;
import org.mobicents.slee.resource.sip11.SipResourceAdaptor;
import org.mobicents.slee.resource.sip11.SleeSipProviderImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.reflect.Whitebox;


public class SipResourceAdaptorTest {

    private static final int SIP_RA1_PORT = 5060;
	private static final int SIP_RA2_PORT = 5059;
	private static final String STACK_ADDRESS = "127.0.0.1";
	// common mocked tracer
	private static Tracer tracer1 = mock(Tracer.class);

	// slee sip provider mock
	private static SleeSipProviderImpl sleeSipProvider = mock(SleeSipProviderImpl.class);

	// minimal mock just to satisfy SipResourceAdaptor's usage of the tracer
	private static void mockTracer() {
		// mock info() call for tracer
		Answer<Object> answer = new Answer<Object>() {
			public Object answer(InvocationOnMock invocation) {
				System.out.println(" (TRACER-MOCK) :: info() called :: " + Arrays.toString(invocation.getArguments()));
				return null;
			}
		};
		doAnswer(answer).when(tracer1).info(anyString());
	}

	// minimal mock just to satisfy SipResourceAdaptor's usage of the tracer
	private void mockRaContext(ResourceAdaptorContext raContext, String response) {
		when(raContext.getEntityName()).thenReturn(response);
	}

	@BeforeClass
	public static void prepareEnvSharedByAllTests() {
		mockTracer();
	}

	// creates common configuration for RA
	private ConfigProperties getCommonProperties() {
		// prepare common configuration for RA
		ConfigProperties configRa = new ConfigProperties();

		configRa.addProperty(new Property("javax.sip.TRANSPORT", "java.lang.String", "TCP"));
		configRa.addProperty(new Property("javax.sip.IP_ADDRESS", "java.lang.String", STACK_ADDRESS));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.BALANCERS", "java.lang.String", ""));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceClassName",
				"java.lang.String", "org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceImpl"));
		return configRa;
	}

	// creates RA instance and populates it with mocked objects
	private SipResourceAdaptor createSipResourceAdaptorInstance(Tracer tracer, ResourceAdaptorContext raContext, SleeSipProviderImpl sleeSipProvider) {
		SipResourceAdaptor ra = new SipResourceAdaptor();

		// initialize internal dependencies of RA#1
		Whitebox.setInternalState(ra, "tracer", tracer);
		Whitebox.setInternalState(ra, "raContext", raContext);
		Whitebox.setInternalState(ra, "providerWrapper", sleeSipProvider);

		return ra;
	}

	@Test
	/*
	 * Purpose of this test it to verify whether two RA entities can be activated on the
	 * same network interface within same class loader.
	 */
	public void testActivateTwoRaEntities() {
		// create two RAs
		ResourceAdaptorContext raContext1 = mock(ResourceAdaptorContext.class);
		ResourceAdaptorContext raContext2 = mock(ResourceAdaptorContext.class);
		mockRaContext(raContext1,"STACK#ActivateTwoRaEntities_1");
		mockRaContext(raContext2,"STACK#ActivateTwoRaEntities_2");

		SipResourceAdaptor ra1 = createSipResourceAdaptorInstance(tracer1,raContext1,sleeSipProvider);
		SipResourceAdaptor ra2 = createSipResourceAdaptorInstance(tracer1,raContext2,sleeSipProvider);

		// prepare configuration for RA#1, RA#2
		ConfigProperties configRa1 = getCommonProperties();
		ConfigProperties configRa2 = getCommonProperties();

		// RA#1 custom configuration
		configRa1.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA1_PORT));
		configRa1.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext1.getEntityName()));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",
			"org.mobicents.ha.javax.sip.RoundRobinLoadBalancerElector"));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
			"org.mobicents.ha.javax.sip.cache.NoCache"));

		// RA#2 custom configuration
		configRa2.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA2_PORT));
		configRa2.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext2.getEntityName()));
		configRa2.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",
				"org.mobicents.ha.javax.sip.RoundRobinLoadBalancerElector"));
		configRa2.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
				"org.mobicents.ha.javax.sip.cache.NoCache"));

		// configure adaptors
		ra1.raConfigure(configRa1);
		try {
			ra1.raVerifyConfiguration(configRa1);
		} catch (InvalidConfigurationException e) {
			fail("Inconsistent configuration fot the test.");
		}

		Assert.assertEquals(SIP_RA1_PORT, Whitebox.getInternalState(ra1, "port"));
		Assert.assertEquals(STACK_ADDRESS, Whitebox.getInternalState(ra1, "stackAddress"));

		ra2.raConfigure(configRa2);

		try {
			ra2.raVerifyConfiguration(configRa2);
		} catch (InvalidConfigurationException e) {
			fail("Inconsistent configuration fot the test.");
		}

		Assert.assertEquals(SIP_RA2_PORT, Whitebox.getInternalState(ra2, "port"));
		Assert.assertEquals(STACK_ADDRESS, Whitebox.getInternalState(ra2, "stackAddress"));

		ra1.raActive();
		ra2.raActive();

		// verify RA1 stack
		SipStackImpl stack1 = Whitebox.getInternalState(ra1, "sipStack");
		Assert.assertEquals(raContext1.getEntityName(),stack1.getStackName());

		// verify that RA1 - stack is listening on a port used in configuration
		Iterator stack1ListeningPoints = stack1.getListeningPoints();
		while (stack1ListeningPoints.hasNext()) {
			ListeningPoint listeningPoint = (ListeningPoint) stack1ListeningPoints.next();
			Assert.assertEquals(SIP_RA1_PORT,listeningPoint.getPort());
		}

		// verify RA2 stack
		SipStackImpl stack2 = Whitebox.getInternalState(ra2, "sipStack");
		Assert.assertEquals(raContext2.getEntityName(),stack2.getStackName());

		// verify that RA2 - stack is listening on a port used in configuration
		Iterator stack2ListeningPoints = stack2.getListeningPoints();
		while (stack2ListeningPoints.hasNext()) {
			ListeningPoint listeningPoint = (ListeningPoint) stack2ListeningPoints.next();
			Assert.assertEquals(SIP_RA2_PORT,listeningPoint.getPort());
		}

		stack1.stop();
		stack2.stop();
    }

    @Test
	/*
	 * Purpose of this test it to verify whether RA raises exception in case invalid location
	 * of sipra.properties is provided, e.g. non-existent file.
	 */
	public void testIncorrectSipRaLocation() {
		ResourceAdaptorContext raContext1 = mock(ResourceAdaptorContext.class);
		mockRaContext(raContext1,"STACK#IncorrectSipRaLocation");

		SipResourceAdaptor ra1 = createSipResourceAdaptorInstance(tracer1,raContext1,sleeSipProvider);

		// prepare configuration for RA#1, RA#2
		ConfigProperties configRa = getCommonProperties();
		configRa.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA1_PORT));
		configRa.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext1.getEntityName()));
		configRa.addProperty(new Property( SipResourceAdaptor.SIPRA_PROPERTIES_LOCATION,
											"java.lang.String",
											"someNonExistingFile.properties"));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",""));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
				"org.mobicents.ha.javax.sip.cache.NoCache"));

		ra1.raConfigure(configRa);
		try {
			ra1.raVerifyConfiguration(configRa);
			fail("raVerifyConfiguration should thrown exception in case of non existing SIP stack properties file.");
		} catch (InvalidConfigurationException e) {
			// expected
		}
	}

	@Test
	/*
	 * Purpose of this test it to verify whether SIP stack uses properties provided in
	 * a dedicated file. Only few of the properties are validated, just to make sure
	 * file was read from given location .
	 */
	public void testValidSipRaLocation() {
		ResourceAdaptorContext raContext1 = mock(ResourceAdaptorContext.class);
		mockRaContext(raContext1,"STACK#ValidSipRaLocation");

		SipResourceAdaptor ra = createSipResourceAdaptorInstance(tracer1,raContext1,sleeSipProvider);

		// we don't know how tests are launched, but need absolute path
		// mind leading / in the resource name
		URL url = getClass().getResource("/sipra-test.properties");
		assertNotNull(url);

		String sipRaPropertiesPath = url.getPath();

		// prepare configuration for RA#1, RA#2
		ConfigProperties configRa = getCommonProperties();
		configRa.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA1_PORT));
		configRa.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext1.getEntityName()));
		configRa.addProperty(new Property( SipResourceAdaptor.SIPRA_PROPERTIES_LOCATION,
				"java.lang.String",
				sipRaPropertiesPath));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",""));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
				"org.mobicents.ha.javax.sip.cache.NoCache"));

		ra.raConfigure(configRa);
		try {
			ra.raVerifyConfiguration(configRa);
		} catch (InvalidConfigurationException e) {
			fail("sipra-test.properties should have been detected correctly");
		}

		// The settings passed in sipra-test.properties are used for SIP stack creation

		// instantiate SIP Stack on RA activation
		ra.raActive();

		// get created sip stack
		SipStackImpl stack = Whitebox.getInternalState(ra, "sipStack");

		// expect values as provided in sample properties file
		//assertEquals("ConfirmedDialog",stack.getReplicationStrategy().toString());
		//assertEquals("org.mobicents.ha.javax.sip.cache.NoCache",stack.getSipCache().getClass().getName());

		SipStackImpl impl = ((org.mobicents.ext.javax.sip.SipStackImpl) stack);
		assertEquals(true,impl.isSendTryingRightAway());

		stack.stop();
	}

	@Test
	/*
	 * Purpose of this test it to verify whether SIP stack uses properties provided in
	 * classpath sipra.properties.
	 */
	public void testNoSipRaLocation() {
		ResourceAdaptorContext raContext1 = mock(ResourceAdaptorContext.class);
		mockRaContext(raContext1,"STACK#NoSipRaLocation");

		SipResourceAdaptor ra = createSipResourceAdaptorInstance(tracer1,raContext1,sleeSipProvider);

		// prepare configuration for RA#1, RA#2
		ConfigProperties configRa = getCommonProperties();
		configRa.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA1_PORT));
		configRa.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext1.getEntityName()));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",""));
		configRa.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
				"org.mobicents.ha.javax.sip.cache.NoCache"));

		ra.raConfigure(configRa);
		try {
			ra.raVerifyConfiguration(configRa);
		} catch (InvalidConfigurationException e) {
			fail("Provided configuration should work just fine or test was not adapted.");
		}

		// The default settings from sipra.properties are used for SIP stack creation

		// instantiate SIP Stack on RA activation
		ra.raActive();

		// get created sip stack
		SipStackImpl stack = Whitebox.getInternalState(ra, "sipStack");

		// expect values as provided in sample properties file
		//assertEquals("EarlyDialog",stack.getReplicationStrategy().toString());
		SipStackImpl impl = ((org.mobicents.ext.javax.sip.SipStackImpl) stack);
		assertEquals(false,impl.isSendTryingRightAway());

		stack.stop();
	}
}
