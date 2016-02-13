package de.m3y3r.ekl;

import java.util.UUID;

import javax.persistence.AttributeConverter;

public class UuidConverter implements AttributeConverter<UUID, byte[]> {

	@Override
	public byte[] convertToDatabaseColumn(UUID attribute) {
		long lsb = attribute.getLeastSignificantBits();
		long msb = attribute.getMostSignificantBits();
		byte[] ba = new byte[] {
				(byte) (lsb >> 56), //0
				(byte) (lsb >> 48), //1
				(byte) (lsb >> 40),
				(byte) (lsb >> 32),
				(byte) (lsb >> 24),
				(byte) (lsb >> 16),
				(byte) (lsb >> 8),
				(byte) lsb,  //7
				(byte) (msb >> 56), //8
				(byte) (msb >> 48),
				(byte) (msb >> 40),
				(byte) (msb >> 32),
				(byte) (msb >> 24),
				(byte) (msb >> 16),
				(byte) (msb >> 8),
				(byte) msb, //15
		};
		return ba;
	}

	@Override
	public UUID convertToEntityAttribute(byte[] dbData) {
		long lsb = dbData[0] >> 56 & 0xff |
				dbData[1] >> 48 & 0xff |
				dbData[2] >> 40 & 0xff |
				dbData[3] >> 32 & 0xff |
				dbData[4] >> 24 & 0xff |
				dbData[5] >> 16 & 0xff |
				dbData[6] >> 8 & 0xff |
				dbData[7] & 0xff;
		long msb = dbData[8] >> 56 & 0xff |
				dbData[9] >> 48 & 0xff |
				dbData[10] >> 40 & 0xff |
				dbData[11] >> 32 & 0xff |
				dbData[12] >> 24 & 0xff |
				dbData[13] >> 16 & 0xff |
				dbData[14] >> 8 & 0xff |
				dbData[15] & 0xff;
		return new UUID(msb, lsb);
	}

}
