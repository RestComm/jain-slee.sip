/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.slee.resource.sip11.wrappers;

import javax.sip.Transaction;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mobicents.slee.resource.sip11.SipResourceAdaptor;

/**
 *
 */
public class TransactionWrapperAppDataTest {

    private static String WRAPPED_APPLICATION_DATA = "Mary";
    private TransactionWrapperAppData transactionWrapperAppData;

    @Before
    public void testBefore() {
        transactionWrapperAppData = new TransactionWrapperAppDataImpl();
    }

    @Test
    public void testWrappedApplicationData() {
        // prepare
        transactionWrapperAppData.setWrappedApplicationData(WRAPPED_APPLICATION_DATA);
        // test
        String wrappedApplicationData = (String) transactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(WRAPPED_APPLICATION_DATA, wrappedApplicationData);
    }

    @Test
    public void testWrappedApplicationDataWhenIsNull() {
        // prepare
        // test
        String wrappedApplicationData = (String) transactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(null, wrappedApplicationData);
    }

    public class TransactionWrapperAppDataImpl extends TransactionWrapperAppData {

        public TransactionWrapper getTransactionWrapper(Transaction transaction, SipResourceAdaptor sipResourceAdaptor) {
            return null;
        }
    }

}
