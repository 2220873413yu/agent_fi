package com.xms.common.utils;

import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.ec.CustomNamedCurves;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.security.Security;
import java.util.Arrays;


public class MetaMaskUtil {

	/**
     * 自定义的签名消息都以以下字符开头
     * 参考 eth_sign in https://github.com/ethereum/wiki/wiki/JSON-RPC
     */
    public static final String PERSONAL_MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";
	private static final X9ECParameters CURVE_PARAMS;
	private static final BigInteger CURVE_ORDER;
	private static final ECCurve CURVE;

	static {
		// 添加BouncyCastle提供者
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(new BouncyCastleProvider());
		}

		// 获取secp256k1曲线参数
		CURVE_PARAMS = CustomNamedCurves.getByName("secp256k1");
		if (CURVE_PARAMS == null) {
			throw new RuntimeException("无法加载secp256k1曲线参数");
		}
		CURVE_ORDER = CURVE_PARAMS.getN();
		CURVE = CURVE_PARAMS.getCurve();
	}

    public static void main(String[] args) {
		// 数据经过处理,使用你自己的地址是可以的
        //签名的地址
        String address = "0xb91a210dbb19ef9ea0c538c2468334189ed29b54";
        //签名后的数据
        String signature = "0xddedb9e221a2098f7698ce3c06518f74221d51940bf0a468512dfae474e4a6c87f522749646e2607e01c8b62b8573d3863f2f7f6ac569053555d7264b42635e31c";
        //签名原文
        String message = "a324df4d343a408c";
        Boolean result = validate(signature,message,address);
        System.out.println(result);
    }

	/**
	 * 验证BSC链签名
	 * @param message   原始消息
	 * @param signature 签名（130字符，格式：0x + 64字符r + 64字符s + 2字符v）
	 * @param address   BSC地址
	 * @return 验证结果
	 */
	public static boolean verify(String message, String signature, String address ) {
		try {
			// 验证参数格式
			if (address == null || signature == null || message == null) {
				return false;
			}

			// 移除0x前缀
			String normalizedAddress = address.startsWith("0x") ? address.substring(2) : address;
			String normalizedSignature = signature.startsWith("0x") ? signature.substring(2) : signature;

			// 验证签名长度（应该是130字符，r(64) + s(64) + v(2) = 130）
			if (normalizedSignature.length() != 130) {
				return false;
			}

			// 提取r、s、v值
			String rHex = normalizedSignature.substring(0, 64);
			String sHex = normalizedSignature.substring(64, 128);
			String vHex = normalizedSignature.substring(128, 130);

			BigInteger r = new BigInteger(rHex, 16);
			BigInteger s = new BigInteger(sHex, 16);
			int v = Integer.parseInt(vHex, 16);

			// 构建以太坊消息前缀（EIP-191标准）
			String prefix = "\u0019Ethereum Signed Message:\n" + message.length();
			String prefixedMessage = prefix + message;

			// 计算消息哈希（Keccak-256）
			byte[] messageHash = keccak256(prefixedMessage.getBytes("UTF-8"));
			BigInteger messageHashInt = new BigInteger(1, messageHash);

			// 计算恢复ID
			int recoveryId = v - 27;
			if (recoveryId < 0 || recoveryId > 3) {
				return false;
			}

			// 从签名恢复公钥
			ECPoint publicKeyPoint = recoverPublicKey(r, s, messageHashInt, recoveryId);
			if (publicKeyPoint == null) {
				return false;
			}

			// 从公钥计算地址
			byte[] publicKeyBytes = publicKeyPoint.getEncoded(false);
			String recoveredAddress = publicKeyToAddress(publicKeyBytes);

			// 比较地址（不区分大小写）
			return normalizedAddress.equalsIgnoreCase(recoveredAddress.substring(2));
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 从公钥字节计算BSC地址
	 *
	 * @param publicKeyBytes 公钥字节（未压缩格式，65字节，第一个字节是0x04）
	 * @return BSC地址
	 */
	private static String publicKeyToAddress(byte[] publicKeyBytes) {
		// 移除第一个字节（0x04，表示未压缩公钥）
		byte[] publicKey = new byte[64];
		System.arraycopy(publicKeyBytes, 1, publicKey, 0, 64);

		// 计算Keccak-256哈希
		byte[] hash = keccak256(publicKey);

		// 取最后20字节作为地址
		byte[] addressBytes = new byte[20];
		System.arraycopy(hash, hash.length - 20, addressBytes, 0, 20);

		return "0x" + byteArrayToHexString(addressBytes);
	}

	/**
	 * Keccak-256哈希计算
	 */
	private static byte[] keccak256(byte[] input) {
		Keccak.DigestKeccak keccak = new Keccak.Digest256();
		keccak.update(input);
		return keccak.digest();
	}

	/**
	 * 字节数组转十六进制字符串
	 */
	private static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	/**
	 * 计算椭圆曲线点的y坐标
	 */
	private static BigInteger getYCoordinate(BigInteger x, boolean isOdd) {
		try {
			// 获取有限域的模数 p
			BigInteger p = CURVE.getField().getCharacteristic();

			// 确保 x 在有限域内
			x = x.mod(p);

			// y^2 = x^3 + ax + b (mod p)
			org.bouncycastle.math.ec.ECFieldElement xField = CURVE.fromBigInteger(x);
			org.bouncycastle.math.ec.ECFieldElement alpha = xField.square().multiply(xField)
				.add(CURVE.getA().multiply(xField)).add(CURVE.getB());
			org.bouncycastle.math.ec.ECFieldElement beta = alpha.sqrt();
			if (beta == null) {
				return null;
			}
			BigInteger yValue = beta.toBigInteger();
			if (yValue.testBit(0) != isOdd) {
				yValue = p.subtract(yValue);
			}
			return yValue;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 从签名恢复公钥
	 *
	 * @param r          签名r值
	 * @param s          签名s值
	 * @param messageHash 消息哈希
	 * @param recoveryId 恢复ID（0-3）
	 * @return 公钥点，如果恢复失败返回null
	 */
	private static ECPoint recoverPublicKey(BigInteger r, BigInteger s, BigInteger messageHash, int recoveryId) {
		try {
			// 计算 x = r + j * n，其中 j 从 0 到 h-1（h 通常是 1）
			BigInteger x = r;
			if (recoveryId >= 2) {
				x = x.add(CURVE_ORDER.multiply(BigInteger.valueOf(recoveryId / 2)));
			}

			// 计算 y 坐标
			BigInteger y = getYCoordinate(x, recoveryId % 2 == 1);
			if (y == null) {
				return null;
			}

			// 创建R点
			ECPoint R = CURVE.validatePoint(x, y);
			if (R == null) {
				return null;
			}

			// 计算 e = messageHash mod n
			BigInteger e = messageHash.mod(CURVE_ORDER);

			// 计算 rInv = r^(-1) mod n
			BigInteger rInv = r.modInverse(CURVE_ORDER);

			// 计算公钥: Q = rInv * (s * R - e * G)
			ECPoint Q = R.multiply(s)
				.subtract(CURVE_PARAMS.getG().multiply(e))
				.multiply(rInv);

			return Q;
		} catch (Exception ex) {
			return null;
		}
	}

    /**
     * 对签名消息，原始消息，账号地址三项信息进行认证，判断签名是否有效
     * @param signature
     * @param message
     * @param address
     * @return
     */
    public static boolean validate(String signature, String message, String address) {
        boolean match = false;
        try {
            //参考 eth_sign in https://github.com/ethereum/wiki/wiki/JSON-RPC
            // eth_sign
            // The sign method calculates an Ethereum specific signature with:
            //    sign(keccak256("\x19Ethereum Signed Message:\n" + len(message) + message))).
            //
            // By adding a prefix to the message makes the calculated signature recognisable as an Ethereum specific signature.
            // This prevents misuse where a malicious DApp can sign arbitrary data (e.g. transaction) and use the signature to
            // impersonate the victim.

            String prefix = PERSONAL_MESSAGE_PREFIX + message.length();
            byte[] msgHash = Hash.sha3((prefix + message).getBytes());

            byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
            byte v = signatureBytes[64];
            if (v < 27) {
                v += 27;
            }

            Sign.SignatureData sd = new Sign.SignatureData(
                    v,
                    Arrays.copyOfRange(signatureBytes, 0, 32),
                    Arrays.copyOfRange(signatureBytes, 32, 64));

            String addressRecovered = null;

            // Iterate for each possible key to recover
            for (int i = 0; i < 4; i++) {
                BigInteger publicKey = Sign.recoverFromSignature(
                        (byte) i,
                        new ECDSASignature(new BigInteger(1, sd.getR()), new BigInteger(1, sd.getS())),
                        msgHash);

                if (publicKey != null) {
                    addressRecovered = "0x" + Keys.getAddress(publicKey);

                    if (addressRecovered.equals(address)) {
                        match = true;
                        break;
                    }
                }
            }
            return match;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
