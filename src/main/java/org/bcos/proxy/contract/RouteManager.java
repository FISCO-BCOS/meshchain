package org.bcos.proxy.contract;

import org.bcos.channel.client.TransactionSucCallback;
import org.bcos.web3j.abi.EventEncoder;
import org.bcos.web3j.abi.EventValues;
import org.bcos.web3j.abi.FunctionEncoder;
import org.bcos.web3j.abi.TypeReference;
import org.bcos.web3j.abi.datatypes.Address;
import org.bcos.web3j.abi.datatypes.Bool;
import org.bcos.web3j.abi.datatypes.Event;
import org.bcos.web3j.abi.datatypes.Function;
import org.bcos.web3j.abi.datatypes.Type;
import org.bcos.web3j.abi.datatypes.Utf8String;
import org.bcos.web3j.abi.datatypes.generated.Bytes32;
import org.bcos.web3j.abi.datatypes.generated.Uint256;
import org.bcos.web3j.crypto.Credentials;
import org.bcos.web3j.protocol.Web3j;
import org.bcos.web3j.protocol.core.DefaultBlockParameter;
import org.bcos.web3j.protocol.core.methods.request.EthFilter;
import org.bcos.web3j.protocol.core.methods.response.Log;
import org.bcos.web3j.protocol.core.methods.response.TransactionReceipt;
import org.bcos.web3j.tx.Contract;
import org.bcos.web3j.tx.TransactionManager;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import rx.Observable;
import rx.functions.Func1;

/**
 * Auto generated code.<br>
 * <strong>Do not modify!</strong><br>
 * Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>, or {@link org.bcos.web3j.codegen.SolidityFunctionWrapperGenerator} to update.
 *
 * <p>Generated with web3j version none.
 */
public final class RouteManager extends Contract {
    private static final String BINARY = "6060604052341561000f57600080fd5b604051610f9a380380610f9a83398101604052808051906020019091908051820191905050816000819055508060019080519060200190610051929190610059565b5050506100fe565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061009a57805160ff19168380011785556100c8565b828001600101855582156100c8579182015b828111156100c75782518255916020019190600101906100ac565b5b5090506100d591906100d9565b5090565b6100fb91905b808211156100f75760008160009055506001016100df565b5090565b90565b610e8d8061010d6000396000f30060606040523615610097576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806308ab47221461009c5780637ba9d980146100c55780637dfbc29114610153578063814844e5146101b6578063cb113bf7146101f5578063e50313eb14610263578063e9207600146102df578063f9467b8b14610325578063faee20461461034e575b600080fd5b34156100a757600080fd5b6100af6103ea565b6040518082815260200191505060405180910390f35b34156100d057600080fd5b6100d86103f7565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156101185780820151818401526020810190506100fd565b50505050905090810190601f1680156101455780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561015e57600080fd5b6101746004808035906020019091905050610495565b604051808273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200191505060405180910390f35b34156101c157600080fd5b6101db6004808035600019169060200190919050506104d4565b604051808215151515815260200191505060405180910390f35b341561020057600080fd5b61021660048080359060200190919050506109c7565b60405180831515151581526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019250505060405180910390f35b341561026e57600080fd5b6102dd600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001908201803590602001908080601f01602080910402602001604051908101604052809392919081815260200183838082843782019150505050505091905050610a2b565b005b34156102ea57600080fd5b610304600480803560001916906020019091905050610b58565b60405180831515151581526020018281526020019250505060405180910390f35b341561033057600080fd5b610338610c2e565b6040518082815260200191505060405180910390f35b341561035957600080fd5b61036f6004808035906020019091905050610c34565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156103af578082015181840152602081019050610394565b50505050905090810190601f1680156103dc5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b6000600280549050905090565b60018054600181600116156101000203166002900480601f01602080910402602001604051908101604052809291908181526020018280546001816001161561010002031660029004801561048d5780601f106104625761010080835404028352916020019161048d565b820191906000526020600020905b81548152906001019060200180831161047057829003601f168201915b505050505081565b6002818154811015156104a457fe5b90600052602060002090016000915054906101000a900473ffffffffffffffffffffffffffffffffffffffff1681565b600080600080600080600060046000896000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b01905090815260200160405180910390205411156105e1577f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a6001600460008a6000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205460405180831515151581526020018281526020019250505060405180910390a1600195506109bd565b6002805490509450600093505b6002805490508410156106da5760028481548110151561060a57fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1692508273ffffffffffffffffffffffffffffffffffffffff1663babd3d9a6000604051602001526040518163ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401602060405180830381600087803b15156106a457600080fd5b6102c65a03f115156106b557600080fd5b5050506040518051905015156106cd578394506106da565b83806001019450506105ee565b60028054905085101515610737577f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a600061276660405180831515151581526020018281526020019250505060405180910390a1600095506109bd565b60028581548110151561074657fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1691508173ffffffffffffffffffffffffffffffffffffffff1663814844e5886000604051602001526040518263ffffffff167c0100000000000000000000000000000000000000000000000000000000028152600401808260001916600019168152602001915050602060405180830381600087803b15156107f357600080fd5b6102c65a03f1151561080457600080fd5b5050506040518051905090508015610972574360046000896000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b0190509081526020016040518091039020819055508460046000896000191660001916815260200190815260200160002060405180807f736574696400000000000000000000000000000000000000000000000000000081525060050190509081526020016040518091039020819055507f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a6001600460008a6000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205460405180831515151581526020018281526020019250505060405180910390a1600195506109bd565b7f651c611909d03094401f55cb12a30384f6d08f9e55297debdfdf531cf38aa78a600061276760405180831515151581526020018281526020019250505060405180910390a1600095505b5050505050919050565b600080600280549050831015156109e45760008091509150610a26565b60016002848154811015156109f557fe5b906000526020600020900160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16915091505b915091565b60028054806001018281610a3f9190610cf0565b9160005260206000209001600084909190916101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055505060038054806001018281610aa29190610d1c565b916000526020600020900160008390919091509080519060200190610ac8929190610d48565b50508173ffffffffffffffffffffffffffffffffffffffff1663d0e0ba956001600280549050036040518263ffffffff167c010000000000000000000000000000000000000000000000000000000002815260040180828152602001915050600060405180830381600087803b1515610b4057600080fd5b6102c65a03f11515610b5157600080fd5b5050505050565b600080600060046000856000191660001916815260200190815260200160002060405180807f626c6f636b6e756d626572000000000000000000000000000000000000000000815250600b0190509081526020016040518091039020541115610c1e57600160046000856000191660001916815260200190815260200160002060405180807f7365746964000000000000000000000000000000000000000000000000000000815250600501905090815260200160405180910390205491509150610c29565b600080809050915091505b915091565b60005481565b600381815481101515610c4357fe5b90600052602060002090016000915090508054600181600116156101000203166002900480601f016020809104026020016040519081016040528092919081815260200182805460018160011615610100020316600290048015610ce85780601f10610cbd57610100808354040283529160200191610ce8565b820191906000526020600020905b815481529060010190602001808311610ccb57829003601f168201915b505050505081565b815481835581811511610d1757818360005260206000209182019101610d169190610dc8565b5b505050565b815481835581811511610d4357818360005260206000209182019101610d429190610ded565b5b505050565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f10610d8957805160ff1916838001178555610db7565b82800160010185558215610db7579182015b82811115610db6578251825591602001919060010190610d9b565b5b509050610dc49190610dc8565b5090565b610dea91905b80821115610de6576000816000905550600101610dce565b5090565b90565b610e1691905b80821115610e125760008181610e099190610e19565b50600101610df3565b5090565b90565b50805460018160011615610100020316600290046000825580601f10610e3f5750610e5e565b601f016020900490600052602060002090810190610e5d9190610dc8565b5b505600a165627a7a723058202807ebf44af863f3c9f6f1948e2a90ff76d0378ed4ecc253619975ec2a0dc8570029";

    private RouteManager(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    private RouteManager(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static List<RegisterRetLogEventResponse> getRegisterRetLogEvents(TransactionReceipt transactionReceipt) {
        final Event event = new Event("registerRetLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        List<EventValues> valueList = extractEventParameters(event, transactionReceipt);
        ArrayList<RegisterRetLogEventResponse> responses = new ArrayList<RegisterRetLogEventResponse>(valueList.size());
        for (EventValues eventValues : valueList) {
            RegisterRetLogEventResponse typedResponse = new RegisterRetLogEventResponse();
            typedResponse.ok = (Bool) eventValues.getNonIndexedValues().get(0);
            typedResponse.set = (Uint256) eventValues.getNonIndexedValues().get(1);
            responses.add(typedResponse);
        }
        return responses;
    }

    public Observable<RegisterRetLogEventResponse> registerRetLogEventObservable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        final Event event = new Event("registerRetLog", 
                Arrays.<TypeReference<?>>asList(),
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(event));
        return web3j.ethLogObservable(filter).map(new Func1<Log, RegisterRetLogEventResponse>() {
            @Override
            public RegisterRetLogEventResponse call(Log log) {
                EventValues eventValues = extractEventParameters(event, log);
                RegisterRetLogEventResponse typedResponse = new RegisterRetLogEventResponse();
                typedResponse.ok = (Bool) eventValues.getNonIndexedValues().get(0);
                typedResponse.set = (Uint256) eventValues.getNonIndexedValues().get(1);
                return typedResponse;
            }
        });
    }

    public Future<Uint256> getSetsNum() {
        Function function = new Function("getSetsNum", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_chainname() {
        Function function = new Function("m_chainname", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Address> m_sets(Uint256 param0) {
        Function function = new Function("m_sets", 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> registerRoute(Bytes32 user) {
        Function function = new Function("registerRoute", Arrays.<Type>asList(user), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void registerRoute(Bytes32 user, TransactionSucCallback callback) {
        Function function = new Function("registerRoute", Arrays.<Type>asList(user), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<List<Type>> getSetAddress(Uint256 idx) {
        Function function = new Function("getSetAddress", 
                Arrays.<Type>asList(idx), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Address>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<TransactionReceipt> registerSet(Address set, Utf8String name) {
        Function function = new Function("registerSet", Arrays.<Type>asList(set, name), Collections.<TypeReference<?>>emptyList());
        return executeTransactionAsync(function);
    }

    public void registerSet(Address set, Utf8String name, TransactionSucCallback callback) {
        Function function = new Function("registerSet", Arrays.<Type>asList(set, name), Collections.<TypeReference<?>>emptyList());
        executeTransactionAsync(function, callback);
    }

    public Future<List<Type>> getRoute(Bytes32 user) {
        Function function = new Function("getRoute", 
                Arrays.<Type>asList(user), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
        return executeCallMultipleValueReturnAsync(function);
    }

    public Future<Uint256> m_chainid() {
        Function function = new Function("m_chainid", 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public Future<Utf8String> m_setNames(Uint256 param0) {
        Function function = new Function("m_setNames", 
                Arrays.<Type>asList(param0), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeCallSingleValueReturnAsync(function);
    }

    public static Future<RouteManager> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 chainid, Utf8String chainname) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(chainid, chainname));
        return deployAsync(RouteManager.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static Future<RouteManager> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger initialWeiValue, Uint256 chainid, Utf8String chainname) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(chainid, chainname));
        return deployAsync(RouteManager.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor, initialWeiValue);
    }

    public static RouteManager load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new RouteManager(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static RouteManager load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new RouteManager(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static class RegisterRetLogEventResponse {
        public Bool ok;

        public Uint256 set;
    }
}
