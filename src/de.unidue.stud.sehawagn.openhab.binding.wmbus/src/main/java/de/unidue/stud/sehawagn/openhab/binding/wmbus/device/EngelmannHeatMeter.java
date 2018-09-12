package de.unidue.stud.sehawagn.openhab.binding.wmbus.device;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Optional;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openmuc.jmbus.DataRecord;
import org.openmuc.jmbus.wireless.WMBusListener;
import org.openmuc.jmbus.wireless.WMBusMessage;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unidue.stud.sehawagn.openhab.binding.wmbus.handler.WMBusDeviceHandler;
import de.unidue.stud.sehawagn.openhab.binding.wmbus.internal.RecordType;

@Component(service = { EngelmannHeatMeter.class }, properties = "OSGI-INF/engelmann.properties")
public class EngelmannHeatMeter extends Meter implements WMBusListener {

    public static final Logger logger = LoggerFactory.getLogger(EngelmannHeatMeter.class);

    public static SecretKey createKey(final String algorithm, final int keysize, final Optional<Provider> provider,
            final Optional<SecureRandom> rng) throws NoSuchAlgorithmException {
        final KeyGenerator keyGenerator;
        if (provider.isPresent()) {
            keyGenerator = KeyGenerator.getInstance(algorithm, provider.get());
        } else {
            keyGenerator = KeyGenerator.getInstance(algorithm);
        }

        if (rng.isPresent()) {
            keyGenerator.init(keysize, rng.get());
        } else {
            // not really needed for the Sun provider which handles null OK
            keyGenerator.init(keysize);
        }

        return keyGenerator.generateKey();
    }

    public static IvParameterSpec createIV(final int ivSizeBytes, final Optional<SecureRandom> rng) {
        final byte[] iv = new byte[ivSizeBytes];
        final SecureRandom theRNG = rng.orElse(new SecureRandom());
        theRNG.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static IvParameterSpec readIV(final int ivSizeBytes, final InputStream is) throws IOException {
        final byte[] iv = new byte[ivSizeBytes];
        int offset = 0;
        while (offset < ivSizeBytes) {
            final int read = is.read(iv, offset, ivSizeBytes - offset);
            if (read == -1) {
                throw new IOException("Too few bytes for IV in input stream");
            }
            offset += read;
        }
        return new IvParameterSpec(iv);
    }

    public static String CHANNEL_CURRENT_VOLUME_INST_VAL;
    private static RecordType TYPE_CURRENT_VOLUME_INST_VAL;

    public class EngelmannHeatMeterHandler extends WMBusDeviceHandler {

        public EngelmannHeatMeterHandler(Thing thing) {
            super(thing);
        }

        @Override
        public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
            logger.trace("handleCommand(): (1/5) command for channel " + channelUID.toString() + " command: "
                    + command.toString());

            if (command == RefreshType.REFRESH) {
                logger.trace("handleCommand(): (2/5) command.refreshtype == REFRESH");
                State newState = UnDefType.NULL;
                if (wmbusDevice != null) {
                    logger.trace("handleCommand(): (3/5) deviceMessage != null");
                    if (CHANNEL_CURRENT_VOLUME_INST_VAL.equals(channelUID.getId())) {
                        logger.trace("handleCommand(): (4/5): got a valid channel: VOLUME_INST_VAL");
                        DataRecord record = wmbusDevice.findRecord(TYPE_CURRENT_VOLUME_INST_VAL);
                        if (record != null) {
                            newState = new DecimalType(record.getScaledDataValue());
                        } else {
                            logger.trace("handleCommand(): record not found in message");
                        }
                    } else {
                        logger.debug("handleCommand(): (4/5): no channel to put this value into found: "
                                + channelUID.getId());
                    }
                    logger.trace("handleCommand(): (5/5) assigning new state to channel '"
                            + channelUID.getId().toString() + "': " + newState.toString());
                    updateState(channelUID.getId(), newState);

                }

            }

        }

    }

    @Override
    public void discardedBytes(byte[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void newMessage(WMBusMessage wmBusMessage) {

        final byte[] decrypted;
        // {
        // final ByteArrayInputStream bais = new ByteArrayInputStream(ciphertext);
        //
        // final Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        // final IvParameterSpec ivForCBC = readIV(aesCBC.getBlockSize(), bais);
        // aesCBC.init(Cipher.DECRYPT_MODE, aesKey, ivForCBC);
        //
        // final byte[] buf = new byte[1_024];
        // try (final CipherInputStream cis = new CipherInputStream(bais, aesCBC);
        // final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        // int read;
        // while ((read = cis.read(buf)) != -1) {
        // baos.write(buf, 0, read);
        // }
        // decrypted = baos.toByteArray();
        // }
        // }

        // wmBusMessage.getVariableDataResponse().decryptMessage(key)

    }

    @Override
    public void stoppedListening(IOException arg0) {
        // TODO Auto-generated method stub

    }
}
