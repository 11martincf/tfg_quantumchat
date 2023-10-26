from hashlib import sha256

def print_as_8(hash):
    out_8 = [int.from_bytes(hash[i:i + 4], "big") for i in range(0, 32, 4)]
    output_string = ""
    for ou in out_8:
        output_string += " " + hex(ou)

    print(output_string)

def build_merkle_tree(commitments, depth):
    tree = []
    empty_leaf = 0
    empty_leaf = sha256(empty_leaf.to_bytes(64, "big")).digest()

    leaf_level = []
    leaf_nodes = pow(2, depth)
    leaf_level += commitments
    leaf_level += [empty_leaf for i in range(leaf_nodes - len(commitments))]
    tree.append(leaf_level)

    for i in range(depth):
        current_level = []
        last_level = tree[i]
        for j in range(pow(2, depth - 1 - i)):
            current_preimage = last_level[2 * j] + last_level[2 * j + 1]
            current_level.append(sha256(current_preimage).digest())
        tree.append(current_level)

    return tree

pubKey = 1234
privKey = 5678
rho = 6666

pubKey_b = pubKey.to_bytes(32, "big")
privKey_b = privKey.to_bytes(32, "big")
rho_b = rho.to_bytes(32, "big")


commitment = pubKey_b + rho_b
commitment_hash = sha256(commitment).digest()
#print_as_8(commitment_hash)

nullifier = privKey_b + rho_b
nullifier_hash = sha256(nullifier).digest()
print_as_8(nullifier_hash)

print()

merkle_tree = build_merkle_tree([commitment_hash], 3)
print_as_8(merkle_tree[0][1])
print_as_8(merkle_tree[1][1])
print_as_8(merkle_tree[2][1])
print_as_8(merkle_tree[3][0])