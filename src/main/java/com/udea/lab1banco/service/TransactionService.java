package com.udea.lab1banco.service;

import com.udea.lab1banco.dto.TransactionDTO;
import com.udea.lab1banco.entity.Customer;
import com.udea.lab1banco.entity.Transaction;
import com.udea.lab1banco.repository.CustomerRepository;
import com.udea.lab1banco.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository; // Para validar cuentas

    public List<TransactionDTO> getAllTransactions() {
        // findAll() es un método mágico de JpaRepository que trae toda la tabla
        List<Transaction> transactions = transactionRepository.findAll();

        // Convertimos la lista de Entidades a lista de DTOs para enviarla al cliente
        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(transaction.getId());
            dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
            dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
            dto.setAmount(transaction.getAmount());
            dto.setTimestamp(transaction.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }



    public TransactionDTO transferMoney(TransactionDTO transactionDTO) {
        // Validar que los números de cuenta no sean nulos
        if (transactionDTO.getSenderAccountNumber() == null || transactionDTO.getReceiverAccountNumber() == null) {
            throw new IllegalArgumentException("Los números de cuenta del remitente y receptor son obligatorios.");
        }

        // Buscar los clientes por número de cuenta
        Customer sender = customerRepository.findByAccountNumber(transactionDTO.getSenderAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del remitente no existe."));
        Customer receiver = customerRepository.findByAccountNumber(transactionDTO.getReceiverAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("La cuenta del receptor no existe."));
//    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found"));
        // Validar que el remitente tenga saldo suficiente
        if (sender.getBalance() < transactionDTO.getAmount()) {
            throw new IllegalArgumentException("Saldo insuficiente en la cuenta del remitente.");
            //throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient balance");
        }

        // Realizar la transferencia
        sender.setBalance(sender.getBalance() - transactionDTO.getAmount());
        receiver.setBalance(receiver.getBalance() + transactionDTO.getAmount());

        // Guardar los cambios en las cuentas
        customerRepository.save(sender);
        customerRepository.save(receiver);


        // Crear y guardar la transacción
        Transaction transaction = new Transaction();
        transaction.setSenderAccountNumber(sender.getAccountNumber());
        transaction.setReceiverAccountNumber(receiver.getAccountNumber());
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setTimestamp(transactionDTO.getTimestamp());

        transaction = transactionRepository.save(transaction);

        // Devolver la transacción creada como DTO
        TransactionDTO savedTransaction = new TransactionDTO();
        savedTransaction.setId(transaction.getId());
        savedTransaction.setSenderAccountNumber(transaction.getSenderAccountNumber());
        savedTransaction.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
        savedTransaction.setAmount(transaction.getAmount());
        savedTransaction.setTimestamp(transaction.getTimestamp());

        return savedTransaction;
    }

    public List<TransactionDTO> getTransactionsByAccount(String accountNumber) {
        List<Transaction> transactions = transactionRepository.findBySenderAccountNumberOrReceiverAccountNumber(accountNumber, accountNumber);
        return transactions.stream().map(transaction -> {
            TransactionDTO dto = new TransactionDTO();
            dto.setId(transaction.getId());
            dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
            dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
            dto.setAmount(transaction.getAmount());
            dto.setTimestamp(transaction.getTimestamp());
            return dto;
        }).collect(Collectors.toList());
    }

   public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));

       TransactionDTO dto = new TransactionDTO();
       dto.setId(transaction.getId());
       dto.setSenderAccountNumber(transaction.getSenderAccountNumber());
       dto.setReceiverAccountNumber(transaction.getReceiverAccountNumber());
       dto.setAmount(transaction.getAmount());
       dto.setTimestamp(transaction.getTimestamp());
       return dto;
   }

    public TransactionDTO updateTransaction(Long id, TransactionDTO transactionDTO) {
        // Primero verificamos que la transacción exista
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transacción no encontrada"));

        // Actualizamos los datos con lo que llegó en el DTO
        existingTransaction.setSenderAccountNumber(transactionDTO.getSenderAccountNumber());
        existingTransaction.setReceiverAccountNumber(transactionDTO.getReceiverAccountNumber());
        existingTransaction.setAmount(transactionDTO.getAmount());
        // Guardamos los cambios en la BD
        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        // Devolvemos el DTO actualizado
        TransactionDTO dto = new TransactionDTO();
        dto.setId(updatedTransaction.getId());
        dto.setSenderAccountNumber(updatedTransaction.getSenderAccountNumber());
        dto.setReceiverAccountNumber(updatedTransaction.getReceiverAccountNumber());
        dto.setAmount(updatedTransaction.getAmount());
        dto.setTimestamp(updatedTransaction.getTimestamp());
        return dto;
    }

    // 6.5 Eliminar una transacción
    public void deleteTransaction(Long id) {
        // Verificamos si existe antes de borrarla
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transacción no encontrada");
        }
        transactionRepository.deleteById(id);
    }

}