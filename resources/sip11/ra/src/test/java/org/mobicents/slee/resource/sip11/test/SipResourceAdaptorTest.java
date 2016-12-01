package org.mobicents.slee.resource.sip11.test;

import javax.sip.ListeningPoint;
import javax.slee.facilities.Tracer;
import javax.slee.resource.ConfigProperties;
import javax.slee.resource.ConfigProperties.Property;
import javax.slee.resource.ResourceAdaptorContext;

import org.mobicents.ha.javax.sip.ClusteredSipStack;
import org.mobicents.slee.resource.sip11.SipResourceAdaptor;
import org.mobicents.slee.resource.sip11.SleeSipProviderImpl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.BeforeClass;

import org.powermock.reflect.Whitebox;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import org.mockito.stubbing.*;
import org.mockito.invocation.*;
import java.util.Arrays;
import java.util.Iterator;

public class SipResourceAdaptorTest {

    private  static final int SIP_RA1_PORT = 5060;
	private static final int SIP_RA2_PORT = 5059;
	private static final String STACK_ADDRESS = "127.0.0.1";
    // common mocked tracer
	private static Tracer tracer1 = mock(Tracer.class);

	// resource adaptor context
	private static ResourceAdaptorContext raContext1 = mock(ResourceAdaptorContext.class);
	private static ResourceAdaptorContext raContext2 = mock(ResourceAdaptorContext.class);

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
	private static void mockRaContext() {
		when(raContext1.getEntityName()).thenReturn("stack#1");
		when(raContext2.getEntityName()).thenReturn("stack#2");
	}

	@BeforeClass
	public static void prepareEnvSharedByAllTests() {
		mockTracer();
		mockRaContext();
	}

	@Test
	/*
	 * Purpose of this test it to verify whether two RA entities can be activated on the
	 * same network interface within same class loader.
	 */
	public void testActivateTwoRaEntities() {
		// create two RAs
		SipResourceAdaptor ra1 = new SipResourceAdaptor();
		SipResourceAdaptor ra2 = new SipResourceAdaptor();

		// initialize internal dependencies of RA#1
		Whitebox.setInternalState(ra1, "tracer", tracer1);
		Whitebox.setInternalState(ra1, "raContext", raContext1);
		Whitebox.setInternalState(ra1, "providerWrapper", sleeSipProvider);

		// initialize internal dependencies of RA#2
		Whitebox.setInternalState(ra2, "tracer", tracer1);
		Whitebox.setInternalState(ra2, "raContext", raContext2);
		Whitebox.setInternalState(ra2, "providerWrapper", sleeSipProvider);

		// prepare configuration for RA#1, RA#2
		ConfigProperties configRa1 = new ConfigProperties();
		ConfigProperties configRa2;

		configRa1.addProperty(new Property("javax.sip.TRANSPORT", "java.lang.String", "TCP"));
		configRa1.addProperty(new Property("javax.sip.IP_ADDRESS", "java.lang.String", STACK_ADDRESS));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.BALANCERS", "java.lang.String", ""));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceClassName",
				"java.lang.String", "org.mobicents.ha.javax.sip.LoadBalancerHeartBeatingServiceImpl"));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.LoadBalancerElector", "java.lang.String",
				"org.mobicents.ha.javax.sip.RoundRobinLoadBalancerElector"));
		configRa1.addProperty(new Property("org.mobicents.ha.javax.sip.CACHE_CLASS_NAME", "java.lang.String",
				"org.mobicents.ha.javax.sip.cache.NoCache"));

		// clone common properties
		configRa2 = (ConfigProperties) configRa1.clone();

		// RA#1 custom configuration
		configRa1.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA1_PORT));
		configRa1.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext1.getEntityName()));

		// RA#2 custom configuration
		configRa2.addProperty(new Property("javax.sip.PORT", "java.lang.Integer", SIP_RA2_PORT));
		configRa2.addProperty(new Property("javax.sip.STACK_NAME", "java.lang.String", raContext2.getEntityName()));

		// configure adaptors
		ra1.raConfigure(configRa1);
		Assert.assertEquals(SIP_RA1_PORT, Whitebox.getInternalState(ra1, "port"));
		Assert.assertEquals(STACK_ADDRESS, Whitebox.getInternalState(ra1, "stackAddress"));

		ra2.raConfigure(configRa2);
		Assert.assertEquals(SIP_RA2_PORT, Whitebox.getInternalState(ra2, "port"));
		Assert.assertEquals(STACK_ADDRESS, Whitebox.getInternalState(ra2, "stackAddress"));

		ra1.raActive();
		ra2.raActive();

		// verify RA1 stack
		ClusteredSipStack stack1 = Whitebox.getInternalState(ra1, "sipStack");
		Assert.assertEquals(raContext1.getEntityName(),stack1.getStackName());

		// verify that RA1 - stack is listening on a port used in configuration
		Iterator stack1ListeningPoints = stack1.getListeningPoints();
		while (stack1ListeningPoints.hasNext()) {
			ListeningPoint listeningPoint = (ListeningPoint) stack1ListeningPoints.next();
			Assert.assertEquals(SIP_RA1_PORT,listeningPoint.getPort());
		}

		// verify RA2 stack
		ClusteredSipStack stack2 = Whitebox.getInternalState(ra2, "sipStack");
		Assert.assertEquals(raContext2.getEntityName(),stack2.getStackName());

		// verify that RA2 - stack is listening on a port used in configuration
		Iterator<ListeningPoint> stack2ListeningPoints = stack1.getListeningPoints();
		while (stack1ListeningPoints.hasNext()) {
			ListeningPoint listeningPoint = (ListeningPoint) stack1ListeningPoints.next();
			Assert.assertEquals(SIP_RA2_PORT,listeningPoint.getPort());
		}
    }
}
