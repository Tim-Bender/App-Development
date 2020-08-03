/*
 *
 *  Copyright (c) 2020, Spudnik LLc <https://www.spudnik.com/>
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are not permitted in any form.
 *
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION, DEATH, or SERIOUS INJURY or DAMAGE)
 *  HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.Diagnostic.Spudnik.Bluetooth;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

public class Operation {

    private final UUID operationUUID;
    private final int OPERATION;
    private Packet writeValue;

    public final static int DISCOVER_SERVICES = 0, READ_CHARACTERISTIC = 1, WRITE_CHARACTERISTIC = 2,
            DISCONNECT = 3, CLOSE_CONNECTION = 4, REQUEST_MTU = 5, READ_RSSI = 6;

    public Operation(@Nullable UUID uuid, @NonNull int operation, @Nullable Packet writeValue) {
        operationUUID = uuid;
        OPERATION = operation;
        this.writeValue = writeValue;
    }

    public UUID getOperationUUID() {
        return operationUUID;
    }

    public int getOPERATION() {
        return OPERATION;
    }

    public Packet getWriteValue() {
        return writeValue;
    }

    public void setWriteValue(Packet writeValue) {
        this.writeValue = writeValue;
    }

}
