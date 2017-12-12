package org.bcos.proxy.contract;

import org.bcos.web3j.abi.FunctionEncoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.tx.Contract;
import org.bcos.web3j.tx.TransactionManager;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.Future;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.bcos.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version none.
 */
public final class Node extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b6040516108d93803806108d98339810160405280805182019190602001805182019190602001805190602001909190805190602001909190805190602001909190805182019190602001805182019190602001805182019190505087600090805190602001906100809291906100ff565b5086600190805190602001906100979291906100ff565b5085600281905550846003819055508360048190555082600590805190602001906100c39291906100ff565b5081600690805190602001906100da9291906100ff565b5080600790805190602001906100f19291906100ff565b5050505050505050506101a4565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061014057805160ff191683800117855561016e565b8280016001018555821561016e579182015b8281111561016d578251825591602001919060010190610152565b5b50905061017b919061017f565b5090565b6101a191905b8082111561019d576000816000905550600101610185565b5090565b90565b610726806101b36000396000f3006060604052361561008c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806337635b6d146100915780633b686c26146100ba5780634f97cb6214610148578063641ba169146101d657806379c73adc14610264578063d3f8de13146102f2578063eb9856691461031b578063ec77bab614610344575b600080fd5b341561009c57600080fd5b6100a46103d2565b6040518082815260200191505060405180910390f35b34156100c557600080fd5b6100cd6103d8565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561010d5780820151818401526020810190506100f2565b50505050905090810190601f16801561013a5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561015357600080fd5b61015b610476565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561019b578082015181840152602081019050610180565b50505050905090810190601f1680156101c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156101e157600080fd5b6101e9610514565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561022957808201518184015260208101905061020e565b50505050905090810190601f1680156102565780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561026f57600080fd5b6102776105b2565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156102b757808201518184015260208101905061029c565b50505050905090810190601f1680156102e45780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156102fd57600080fd5b610305610650565b6040518082815260200191505060405180910390f35b341561032657600080fd5b61032e610656565b6040518082815260200191505060405180910390f35b341561034f57600080fd5b61035761065c565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561039757808201518184015260208101905061037c565b50505050905090810190601f1680156103c45780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b60035481565b60068054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561046e5780601f106104435761010080835404028352916020019161046e565b820191906000526020600020905b81548152906001019060200180831161045157829003601f168201915b505050505081565b60018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561050c5780601f106104e15761010080835404028352916020019161050c565b820191906000526020600020905b8154815290600101906020018083116104ef57829003601f168201915b505050505081565b60058054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156105aa5780601f1061057f576101008083540402835291602001916105aa565b820191906000526020600020905b81548152906001019060200180831161058d57829003601f168201915b505050505081565b60008054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106485780601f1061061d57610100808354040283529160200191610648565b820191906000526020600020905b81548152906001019060200180831161062b57829003601f168201915b505050505081565b60025481565b60045481565b60078054600181600116156101000203166002900480601f0160208091040260200160405190810160405280929190818152602001828054600181600116156101000203166002900480156106f25780601f106106c7576101008083540402835291602001916106f2565b820191906000526020600020905b8154815290600101906020018083116106d557829003601f168201915b5050505050815600a165627a7a723058200830f7d4cc5d10e3ccd1b1fb18164f8e6f5ce95aaee6b5efcdf587239eee4ab00029";

    private Node(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private Node(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public Future<Uint256> m_rpcport() {
        Function function = new Function("m_rpcport", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_cahash() {
        Function function = new Function("m_cahash", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_ip() {
        Function function = new Function("m_ip", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_desc() {
        Function function = new Function("m_desc", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_id() {
        Function function = new Function("m_id", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> m_p2pport() {
        Function function = new Function("m_p2pport", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Uint256> m_type() {
        Function function = new Function("m_type", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_agencyinfo() {
        Function function = new Function("m_agencyinfo", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<Node> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Utf8String id, Utf8String ip, Uint256 p2pport, Uint256 rpcport, Uint256 t, Utf8String desc, Utf8String cahash, Utf8String agencyinfo) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(id, ip, p2pport, rpcport, t, desc, cahash, agencyinfo));
        return deployAsync(Node.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Future<Node> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Utf8String id, Utf8String ip, Uint256 p2pport, Uint256 rpcport, Uint256 t, Utf8String desc, Utf8String cahash, Utf8String agencyinfo) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(id, ip, p2pport, rpcport, t, desc, cahash, agencyinfo));
        return deployAsync(Node.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Node load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new Node(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static Node load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new Node(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
