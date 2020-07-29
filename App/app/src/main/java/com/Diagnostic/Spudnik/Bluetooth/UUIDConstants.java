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

import java.util.UUID;

@SuppressWarnings("unused")
public enum UUIDConstants {
    SERVICE_UUID(UUID.fromString("ea7f12aa-0fa1-658b-bd4a-78b9c758fdbb")), //main service uuid
    SEND_UPDATE_UUID(UUID.fromString("1d354544-8c53-2ba3-7646-2e4225d4596a")), //update software - disabled
    SYSTEM_INFORMATION(UUID.fromString("153629f4-23fc-35b3-ec40-047a7292ca5b")), //version...
    WRITE_SENSOR_CONFIGURATION(UUID.fromString("77fc6fe4-26a3-c1a0-694c-428f97bc3176")), //write config in 1-4
    READ_SENSOR_STATUS(UUID.fromString("1ec79834-e66c-7fb0-8241-b41b94c8ba73")), //read in 1-4\
    WRITE_STANDARD_CONTROL_CONFIGURATION(UUID.fromString("adbf2537-ef53-0a9f-a443-ce46702af9b9")), //writes config out1,2,3
    READ_STANDARD_CONTROL_STATUS(UUID.fromString("a6949c00-d6b0-fc95-8943-5fdd489fa949")), //read out1,2,3
    WRITE_SPECIAL_CONTROL_CONFIGURATION(UUID.fromString("d0fede9c-f89a-0ab0-1644-f994afeadd03")), //write config out 4-9
    READ_SPECIAL_CONTROL_STATUS(UUID.fromString("79723e9f-70fb-2391-0a44-5ab4fc922b96")); //read out 4-9

    private final UUID fromString;

    UUIDConstants(UUID fromString) {
        this.fromString = fromString;
    }

    public UUID getFromString() {
        return fromString;
    }
}
