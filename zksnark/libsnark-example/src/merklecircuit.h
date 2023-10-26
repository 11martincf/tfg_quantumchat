#ifndef MERKLECIRCUIT_H
#define MERKLECIRCUIT_H

#include <libff/common/default_types/ec_pp.hpp>
#include <libsnark/zk_proof_systems/ppzksnark/r1cs_gg_ppzksnark/r1cs_gg_ppzksnark.hpp>
#include <libsnark/gadgetlib1/gadgets/merkle_tree/merkle_tree_check_read_gadget.hpp>
#include <libsnark/gadgetlib1/gadgets/hashes/sha256/sha256_gadget.hpp>

using namespace libsnark;
namespace sample {
    template<typename FieldT, typename HashT>
    class MerkleCircuit
    {
    public:
        const size_t digest_size;
        const size_t tree_depth;
        std::shared_ptr<digest_variable<FieldT>> root_digest;
        std::shared_ptr<block_variable<FieldT>> pubKey_and_sigma_block;
        std::shared_ptr<block_variable<FieldT>> privKey_and_sigma_block;
        std::shared_ptr<digest_variable<FieldT>> commitment_digest;
        std::shared_ptr<digest_variable<FieldT>> nullifier_digest;
        pb_variable_array<FieldT> address_bits_var;
        std::shared_ptr<merkle_authentication_path_variable<FieldT, HashT>> path_var;
        std::shared_ptr<merkle_tree_check_read_gadget<FieldT, HashT>> merkle;
        std::shared_ptr<HashT> commitment_check;
        std::shared_ptr<HashT> nullifier_check;

        MerkleCircuit(protoboard<FieldT>& pb, const size_t& depth):
                digest_size(HashT::get_digest_len()),
                tree_depth(depth)
        {
            root_digest = std::make_shared<digest_variable<FieldT>>(pb, digest_size, "root");
            nullifier_digest = std::make_shared<digest_variable<FieldT>>(pb, digest_size, "nullifier");
            commitment_digest = std::make_shared<digest_variable<FieldT>>(pb, digest_size, "commitment");
            pubKey_and_sigma_block = std::make_shared<block_variable<FieldT>>(pb, HashT::get_block_len(), "commitment-preimage");
            privKey_and_sigma_block = std::make_shared<block_variable<FieldT>>(pb, HashT::get_block_len(), "nullifier-preimage");

            path_var = std::make_shared<merkle_authentication_path_variable<FieldT, HashT>>(pb, tree_depth, "path");
            address_bits_var.allocate(pb, tree_depth, "address_bits");
            merkle = std::make_shared<merkle_tree_check_read_gadget<FieldT, HashT>>(pb, tree_depth, address_bits_var, *commitment_digest, *root_digest, *path_var, ONE, "merkle");
            commitment_check = std::make_shared<HashT>(pb, HashT::get_block_len(), *pubKey_and_sigma_block, *commitment_digest, "check-commit");
            nullifier_check = std::make_shared<HashT>(pb, HashT::get_block_len(), *privKey_and_sigma_block, *nullifier_digest, "check-null");
            pb.set_input_sizes(root_digest->digest_size + nullifier_digest->digest_size);
        }

        void generate_r1cs_constraints() {
            path_var->generate_r1cs_constraints();
            merkle->generate_r1cs_constraints();
            commitment_check->generate_r1cs_constraints(true);
            nullifier_check->generate_r1cs_constraints(true);
        }

        void generate_r1cs_witness(protoboard<FieldT>& pb, libff::bit_vector& root, libff::bit_vector& nullifier,
                                   libff::bit_vector& commitment, libff::bit_vector& pubKey_and_sigma,
                                   libff::bit_vector& privKey_and_sigma, merkle_authentication_path& path,
                                   const size_t address, libff::bit_vector& address_bits) {
            root_digest->generate_r1cs_witness(root);
            nullifier_digest->generate_r1cs_witness(nullifier);
            commitment_digest->generate_r1cs_witness(commitment);

            pubKey_and_sigma_block->generate_r1cs_witness(pubKey_and_sigma);
            privKey_and_sigma_block->generate_r1cs_witness(privKey_and_sigma);


            address_bits_var.fill_with_bits(pb, address_bits);
            assert(address_bits_var.get_field_element_from_bits(pb).as_ulong() == address);
            path_var->generate_r1cs_witness(address, path);
            merkle->generate_r1cs_witness();

            commitment_check->generate_r1cs_witness();
            nullifier_check->generate_r1cs_witness();

        }
    };
}
#endif //MERKLECIRCUIT_H