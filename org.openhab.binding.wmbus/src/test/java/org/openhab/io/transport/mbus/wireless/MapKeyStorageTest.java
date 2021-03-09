package org.openhab.io.transport.mbus.wireless;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.openhab.core.util.HexUtils;
import org.openmuc.jmbus.SecondaryAddress;

/**
 * A basic test of encryption key lookups.
 *
 * @author Łukasz Dywicki - Initial contribution.
 */
public class MapKeyStorageTest {

    private static final String ADDRESS_HEX = "2423870723421147";
    private static final byte[] ADDRESS_BYTE = HexUtils.hexToBytes(ADDRESS_HEX);
    private static final SecondaryAddress ADDRESS_OBJECT = SecondaryAddress.newFromWMBusHeader(ADDRESS_BYTE, 0);
    private static final byte[] KEY = new byte[] { 0x01, 0x02 };

    private final MapKeyStorage storage = new MapKeyStorage();

    @Test
    public void testNoKey() {
        miss(ADDRESS_BYTE, ADDRESS_OBJECT);
    }

    @Test
    public void tesKeyUpdate() {
        testNoKey();

        storage.registerKey(ADDRESS_BYTE, KEY);

        hit(ADDRESS_BYTE, ADDRESS_OBJECT, KEY);
    }

    protected void miss(byte[] byteForm, SecondaryAddress objectForm) {
        Assertions.assertThat(storage.lookupKey(byteForm)).isNotNull().isEmpty();
        Assertions.assertThat(storage.toMap().get(objectForm)).isNull();
    }

    protected void hit(byte[] byteForm, SecondaryAddress objectForm, byte[] key) {
        Assertions.assertThat(storage.lookupKey(byteForm)).isNotNull().isNotEmpty().hasValue(key);
        Assertions.assertThat(storage.toMap().get(objectForm)).isNotNull().isEqualTo(key);
    }
}
