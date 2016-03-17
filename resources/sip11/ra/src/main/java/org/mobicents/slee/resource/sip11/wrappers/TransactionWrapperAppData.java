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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.sip.Transaction;
import org.mobicents.slee.resource.sip11.SipResourceAdaptor;

public abstract class TransactionWrapperAppData<T extends Transaction, TW extends TransactionWrapper>
        implements WrapperAppData {

    protected TW transactionWrapper;
    private static final long serialVersionUID = 4855074939713665946L;
    private Object wrappedApplicationData;

    public abstract TW getTransactionWrapper(T transaction, SipResourceAdaptor sipResourceAdaptor);

    public TransactionWrapperAppData() {
    }
    
    public TransactionWrapperAppData(TW transactionWrapper) {
        this.transactionWrapper = transactionWrapper;
    }

    /**
     * Gets the wrapped application data.
     *
     * @return
     */
    public Object getWrappedApplicationData() {
        return wrappedApplicationData;
    }

    /**
     * Sets the wrapped application data.
     *
     * @param wrappedApplicationData
     */
    public void setWrappedApplicationData(Object wrappedApplicationData) {
        this.wrappedApplicationData = wrappedApplicationData;
    }

    @Override
    public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        readExternalWrappedApplicationData(objectInput);
    }

    private void readExternalWrappedApplicationData(ObjectInput objectInput) throws IOException, ClassNotFoundException {
        if (objectInput.readBoolean()) {
            wrappedApplicationData = objectInput.readObject();
        }
    }

    @Override
    public void writeExternal(ObjectOutput objectOutput) throws IOException {
        writeExternalWrappedApplicationData(objectOutput);
    }

    private void writeExternalWrappedApplicationData(ObjectOutput objectOutput) throws IOException {
        if (wrappedApplicationData != null) {
            objectOutput.writeBoolean(true);
            objectOutput.writeObject(wrappedApplicationData);
        } else {
            objectOutput.writeBoolean(false);
        }
    }

}
