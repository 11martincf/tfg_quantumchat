//
// Created by davidsoler on 19/04/2023.
//

#ifndef LIBSNARK_TEST_SHA256_EXTRAGADGET_HPP
#define LIBSNARK_TEST_SHA256_EXTRAGADGET_HPP

#include <libsnark/common/data_structures/merkle_tree.hpp>
#include <libsnark/gadgetlib1/gadgets/basic_gadgets.hpp>
#include <libsnark/gadgetlib1/gadgets/hashes/hash_io.hpp>
#include <libsnark/gadgetlib1/gadgets/hashes/sha256/sha256_gadget.hpp>
#include <libsnark/gadgetlib1/gadgets/hashes/sha256/sha256_components.hpp>

namespace libsnark {
    template<typename FieldT>
    class sha256_ethereum : gadget<FieldT> {
    private:
        std::shared_ptr<block_variable<FieldT>> block1;
        std::shared_ptr<block_variable<FieldT>> block2;
        std::shared_ptr<sha256_compression_function_gadget<FieldT>> hasher1;
        std::shared_ptr<digest_variable<FieldT>> intermediate_hash;
        std::shared_ptr<sha256_compression_function_gadget<FieldT>> hasher2;

    public:

        pb_variable_array<FieldT> from_bits(std::vector<bool> bits, pb_variable<FieldT> &ZERO) {
            pb_variable_array<FieldT> acc;

            for (size_t i = 0; i < bits.size(); i++) {
                bool bit = bits[i];
                acc.emplace_back(bit ? ONE : ZERO);
            }

            return acc;
        }

        sha256_ethereum(protoboard<FieldT> &pb,
                        const size_t block_length,
                        const block_variable<FieldT> &input_block,
                        const digest_variable<FieldT> &output,
                        const std::string &annotation_prefix) : gadget<FieldT>(pb, "sha256_ethereum") {

            intermediate_hash.reset(new digest_variable<FieldT>(pb, 256, "intermediate"));
            pb_variable<FieldT> ZERO;

            ZERO.allocate(pb, "ZERO");
            pb.val(ZERO) = 0;

            // final padding
            pb_variable_array<FieldT> length_padding =
                    from_bits({
                                      // padding
                                      1, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,

                                      // length of message (512 bits)
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0,
                                      0, 0, 0, 0, 0, 0, 1, 0,
                                      0, 0, 0, 0, 0, 0, 0, 0
                              }, ZERO);

/*        block2.reset(new block_variable<FieldT>(pb, {
        length_padding
    }, "block2"));
*/
            pb_linear_combination_array<FieldT> IV = SHA256_default_IV(pb);

            hasher1.reset(new sha256_compression_function_gadget<FieldT>(
                    pb,
                    IV,
                    input_block.bits,
                    *intermediate_hash,
                    "hasher1"));

            pb_linear_combination_array<FieldT> IV2(intermediate_hash->bits);
            //      std::cout << block2->bits;
//        std::cout << intermediate_hash;

            hasher2.reset(new sha256_compression_function_gadget<FieldT>(
                    pb,
                    IV2,
                    length_padding,
                    output,
                    "hasher2"));

        }

        void generate_r1cs_constraints(const bool ensure_output_bitness) {
            libff::UNUSED(ensure_output_bitness);
            hasher1->generate_r1cs_constraints();
            hasher2->generate_r1cs_constraints();
        }

        void generate_r1cs_witness() {
            hasher1->generate_r1cs_witness();
            hasher2->generate_r1cs_witness();
        }

        static size_t get_block_len() {
            return SHA256_block_size;
        }

        static size_t get_digest_len() {
            return 256;
        }


        static libff::bit_vector get_hash(const libff::bit_vector &input) {

            protoboard<FieldT> pb;

            block_variable<FieldT> input_variable(pb, SHA256_block_size, "input");
            digest_variable<FieldT> output_variable(pb, SHA256_digest_size, "output");
            sha256_ethereum f(pb, SHA256_block_size, input_variable, output_variable, "f");

            input_variable.generate_r1cs_witness(input);
            f.generate_r1cs_witness();

            return output_variable.get_digest();

        }


        static size_t expected_constraints(const bool ensure_output_bitness) {
            libff::UNUSED(ensure_output_bitness);
            return 54560; /* hardcoded for now */
        }
    };

}
#endif //LIBSNARK_TEST_SHA256_EXTRAGADGET_HPP
