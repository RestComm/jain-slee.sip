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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 */
public class ClientTransactionWrapperAppDataTest {

    private static String WRAPPED_APPLICATION_DATA = "Mary";
    private ClientTransactionWrapperAppData clientTransactionWrapperAppData;

    @Before
    public void testBefore() {
        clientTransactionWrapperAppData = new ClientTransactionWrapperAppData();
    }
    
    @Test
    public void testExternalWhenWrappedApplicationData() throws IOException, ClassNotFoundException {
        // prepare
        clientTransactionWrapperAppData.setWrappedApplicationData(WRAPPED_APPLICATION_DATA);
        // test
        byte[] transactionWrapperAppDataBytes = externalWriteTransactionWrapperAppData();
        ClientTransactionWrapperAppData clientTransactionWrapperAppData = externalReadTransactionWrapperAppData(transactionWrapperAppDataBytes);
        String wrappedApplicationData = (String) clientTransactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(WRAPPED_APPLICATION_DATA, wrappedApplicationData);
    }
    
    @Test
    public void testExternalWhenWrappedApplicationDataIsNull() throws IOException, ClassNotFoundException {
        // prepare
        clientTransactionWrapperAppData.setWrappedApplicationData(null);
        // test
        byte[] transactionWrapperAppDataBytes = externalWriteTransactionWrapperAppData();
        ClientTransactionWrapperAppData clientTransactionWrapperAppData = externalReadTransactionWrapperAppData(transactionWrapperAppDataBytes);
        String wrappedApplicationData = (String) clientTransactionWrapperAppData.getWrappedApplicationData();
        // assert
        assertEquals(null, wrappedApplicationData);
    }

    private ClientTransactionWrapperAppData externalReadTransactionWrapperAppData(byte[] transactionWrapperAppDataBytes)
            throws ClassNotFoundException, IOException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(transactionWrapperAppDataBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        return (ClientTransactionWrapperAppData) objectInputStream.readObject();
    }

    private byte[] externalWriteTransactionWrapperAppData() throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        new ObjectOutputStream(byteArrayOutputStream).writeObject(clientTransactionWrapperAppData);
        return byteArrayOutputStream.toByteArray();
    }

}
